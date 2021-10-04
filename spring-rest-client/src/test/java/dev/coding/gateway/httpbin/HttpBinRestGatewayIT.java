package dev.coding.gateway.httpbin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static dev.coding.config.RestTemplateConfiguration.REST_TEMPLATE_FOR_STRING_DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Qualifier(REST_TEMPLATE_FOR_STRING_DATA)
    @SpyBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpBinRestGateway restGateway;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void beforeEach () {
        final CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("http-bin");
        circuitBreaker.reset();
        WireMock.reset();
    }

    @Test
    void get_returnsResponseEntity_onHttp2xx () {
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok("anyJsonNode()");

        stubForGet(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_returnsResponseEntity_onHttp404 () {
        final ResponseEntity<String> expectedResponseEntity = notFound().build();

        stubForGet(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_throwsRestCallFailedException_onHttp4xx () {
        stubForGet(badRequest().build());

        assertThrows(RestCallFailedException.class, () -> restGateway.get());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void get_throwsRestCallShouldRetryExceptionAndRetries_onRetriableHttpStatus (final int statusCode) {
        stubForGet(ResponseEntity.status(statusCode).build());

        assertThrows(RestCallShouldRetryException.class, () -> restGateway.get());

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_doesNotRetryAndDoesNotOpenCircuitBreakerAndReturnsResponseEntity_onRepeatedHttp404 () {
        stubForGet(notFound().build());

        for (int i = 0; i < 10; i++) {
            final ResponseEntity<String> responseEntity = restGateway.get();
            assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
        }

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void get_opensCircuitBreakerAfterRetriesAndThrowsCallNotPermittedException_onRepeatedRetriableHttpStatus (final int statusCode) {
        stubForGet(ResponseEntity.status(statusCode).build());

        for (int i = 0; i < 3; i++) {
            assertThrows(RestCallShouldRetryException.class, () -> restGateway.get());
        }

        assertThrows(CallNotPermittedException.class, () -> restGateway.get());

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void post_returnsResponseEntity_onHttp2xx () throws JsonProcessingException {
        final String data = "{\"field1\":\"anyValue1\"}";
        final String responseBody = "{\"id\":\"newId\"}";
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok(responseBody);

        stubForPost(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.post(data);
        final JsonNode responseBodyAsJson = toJsonNode(responseEntity.getBody());

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertTrue(responseBody.equalsIgnoreCase(responseBodyAsJson.toString()));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void post_returnsResponseEntity_onHttp404 () {
        final String data = "{\"field1\":\"anyValue1\"}";
        final ResponseEntity<String> expectedResponseEntity = notFound().build();

        stubForPost(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.post(data);

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void post_throwsRestCallFailedException_onHttp4xx () {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPost(badRequest().build());

        assertThrows(RestCallFailedException.class, () -> restGateway.post(data));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void post_throwsRestCallShouldRetryExceptionAndRetries_onRetriableHttpStatus (final int statusCode) {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPost(ResponseEntity.status(statusCode).body(data));

        assertThrows(RestCallShouldRetryException.class, () -> restGateway.post(data));

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void post_doesNotRetryAndDoesNotOpenCircuitBreakerAndReturnsResponseEntity_onRepeatedHttp404 () {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPost(notFound().build());

        for (int i = 0; i < 10; i++) {
            final ResponseEntity<String> responseEntity = restGateway.post(data);
            assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
        }

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void post_opensCircuitBreakerAfterRetriesAndThrowsCallNotPermittedException_onRepeatedRetriableHttpStatus (final int statusCode) {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPost(ResponseEntity.status(statusCode).build());

        for (int i = 0; i < 3; i++) {
            assertThrows(RestCallShouldRetryException.class, () -> restGateway.post(data));
        }

        assertThrows(CallNotPermittedException.class, () -> restGateway.post(data));

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void put_returnsResponseEntity_onHttp2xx () throws JsonProcessingException {
        final String data = "{\"field1\":\"anyValue1\"}";
        final String responseBody = "{\"id\":\"newId\"}";
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok(responseBody);

        stubForPut(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.put(data);
        final JsonNode responseBodyAsJson = toJsonNode(responseEntity.getBody());

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertTrue(responseBody.equalsIgnoreCase(responseBodyAsJson.toString()));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void put_returnsResponseEntity_onHttp404 () {
        final String data = "{\"field1\":\"anyValue1\"}";
        final ResponseEntity<String> expectedResponseEntity = notFound().build();

        stubForPut(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.put(data);

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void put_throwsRestCallFailedException_onHttp4xx () {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPut(badRequest().build());

        assertThrows(RestCallFailedException.class, () -> restGateway.put(data));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void put_throwsRestCallShouldRetryExceptionAndRetries_onRetriableHttpStatus (final int statusCode) {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPut(ResponseEntity.status(statusCode).body(data));

        assertThrows(RestCallShouldRetryException.class, () -> restGateway.put(data));

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void put_doesNotRetryAndDoesNotOpenCircuitBreakerAndReturnsResponseEntity_onRepeatedHttp404 () {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPut(notFound().build());

        for (int i = 0; i < 10; i++) {
            final ResponseEntity<String> responseEntity = restGateway.put(data);
            assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
        }

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500 })
    void put_opensCircuitBreakerAfterRetriesAndThrowsCallNotPermittedException_onRepeatedRetriableHttpStatus (final int statusCode) {
        final String data = "{\"field1\":\"anyValue1\"}";
        stubForPut(ResponseEntity.status(statusCode).build());

        for (int i = 0; i < 3; i++) {
            assertThrows(RestCallShouldRetryException.class, () -> restGateway.put(data));
        }

        assertThrows(CallNotPermittedException.class, () -> restGateway.put(data));

        verify(restTemplate, times(10)).exchange(any(RequestEntity.class), eq(String.class));
    }

    private JsonNode toJsonNode (final String content) throws JsonProcessingException {
        return objectMapper.readValue(content, JsonNode.class);
    }

    private void stubForGet (final ResponseEntity<String> responseEntity) {
        stubForPath(WireMock.get("/get"), responseEntity);
    }

    private void stubForPost (final ResponseEntity<String> responseEntity) {
        stubForPath(WireMock.post("/post"), responseEntity);
    }

    private void stubForPut (final ResponseEntity<String> responseEntity) {
        stubForPath(WireMock.put("/put"), responseEntity);
    }

    private void stubForPath (final MappingBuilder mappingBuilder, final ResponseEntity<String> responseEntity) {
        WireMock.stubFor(mappingBuilder
                .willReturn(responseDefinition()
                        .withStatus(responseEntity.getStatusCodeValue())
                        .withBody(responseEntity.getBody())));
    }
}
