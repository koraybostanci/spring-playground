package dev.coding.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.coding.common.exception.system.rest.RestCallFailedException;
import dev.coding.common.exception.system.rest.RestCallShouldRetryException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;

@WireMockTest(httpPort = 8001)
@SpringBootTest
public class HttpBinRestGatewayIT {

    private static final int RETRY_COUNT = 3;

    @Autowired
    private HttpBinRestGateway restGateway;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @SpyBean
    private RestTemplate restTemplate;

    @BeforeEach
    void beforeEach () {
        final CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("http-bin");
        circuitBreaker.reset();
    }

    @Test
    void get_returnsResponseEntity_onHttp2xx () {
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok("anyBody");

        stubFor("/get", expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_returnsResponseEntity_onHttp404 () {
        final ResponseEntity<String> expectedResponseEntity = notFound().build();

        stubFor("/get", expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_throwsRestCallFailedException_onHttp4xx () {
        stubFor("/get", badRequest().build());

        assertThrows(RestCallFailedException.class, () -> restGateway.get());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void get_throwsRestCallShouldRetryExceptionAndRetries_onRetriableHttpStatus (final int statusCode) {
        stubFor("/get", ResponseEntity.status(statusCode).build());

        assertThrows(RestCallShouldRetryException.class, () -> restGateway.get());

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_doesNotRetryAndDoesNotOpenCircuitBreakerAndReturnsResponseEntity_onRepeatedHttp404 () {
        stubFor("/get", notFound().build());

        for (int i = 0; i < 10; i++) {
            final ResponseEntity<String> responseEntity = restGateway.get();
            assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
        }

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void get_opensCircuitBreakerAfterRetriesAndThrowsCallNotPermittedException_onRepeatedRetriableHttpStatus (final int statusCode) {
        stubFor("/get", ResponseEntity.status(statusCode).build());

        for (int i = 0; i < 3; i++) {
            assertThrows(RestCallShouldRetryException.class, () -> restGateway.get());
        }

        assertThrows(CallNotPermittedException.class, () -> restGateway.get());

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    private void stubFor (final String url, final ResponseEntity<String> responseEntity) {
        WireMock.stubFor(WireMock.get(url)
                .willReturn(responseDefinition()
                        .withStatus(responseEntity.getStatusCodeValue())
                        .withBody(responseEntity.getBody())));
    }
}
