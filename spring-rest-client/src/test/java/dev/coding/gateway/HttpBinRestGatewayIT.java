package dev.coding.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.notFound;

@WireMockTest(httpPort = 8001)
@SpringBootTest
public class HttpBinRestGatewayIT {

    private static final int RETRY_COUNT = 5;

    @Autowired
    private HttpBinRestGateway restGateway;
    @SpyBean
    private RestTemplate restTemplate;

    @Test
    void get_returnsResponseEntity_whenRemoteServiceReturnsHttp2xx () {
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok("anyBody");

        stubFor("/get", expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_throwsHttpClientErrorException_whenRemoteServiceReturnsHttp4xx () {
        stubFor("/get", notFound().build());

        assertThrows(HttpClientErrorException.class, () -> restGateway.get());

        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_retriesAndFinallyThrowsHttpClientErrorException_whenRemoteServiceReturnsHttp429 () {
        stubFor("/get", ResponseEntity.status(TOO_MANY_REQUESTS).build());

        assertThrows(HttpClientErrorException.class, () -> restGateway.get());

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void get_retriesAndFinallyThrowsHttpServerErrorException_whenRemoteServiceReturnsHttp5xx () {
        stubFor("/get", internalServerError().build());

        assertThrows(HttpServerErrorException.class, () -> restGateway.get());

        verify(restTemplate, times(RETRY_COUNT)).exchange(any(RequestEntity.class), eq(String.class));
    }

    private void stubFor (final String url, final ResponseEntity<String> responseEntity) {
        WireMock.stubFor(WireMock.get(url)
                .willReturn(responseDefinition()
                        .withStatus(responseEntity.getStatusCodeValue())
                        .withBody(responseEntity.getBody())));
    }
}
