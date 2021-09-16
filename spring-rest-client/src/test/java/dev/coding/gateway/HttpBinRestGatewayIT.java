package dev.coding.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.notFound;

@WireMockTest(httpPort = 8001)
@SpringBootTest
public class HttpBinRestGatewayIT {

    @Autowired
    private HttpBinRestGateway restGateway;

    @Test
    void get_returnsResponseEntity_whenRemoteServiceReturnsHttp2xx () {
        final ResponseEntity<String> expectedResponseEntity = ResponseEntity.ok("anyBody");

        stubFor("/get", expectedResponseEntity);

        final ResponseEntity<String> responseEntity = restGateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());
    }

    @Test
    void get_throwsHttpClientErrorException_whenRemoteServiceReturnsHttp4xx () {
        stubFor("/get", notFound().build());

        assertThrows(HttpClientErrorException.class, () -> restGateway.get());
    }

    @Test
    void get_throwsHttpServerErrorException_whenRemoteServiceReturnsHttp5xx () {
        stubFor("/get", internalServerError().build());

        assertThrows(HttpServerErrorException.class, () -> restGateway.get());
    }

    private void stubFor (final String url, final ResponseEntity<String> responseEntity) {
        WireMock.stubFor(WireMock.get(url)
                .willReturn(responseDefinition()
                        .withStatus(responseEntity.getStatusCodeValue())
                        .withBody(responseEntity.getBody())));
    }
}
