package dev.coding.gateway;

import dev.coding.config.RestClientConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@ExtendWith(MockitoExtension.class)
public class HttpBinRestGatewayTest {

    private static final String BASE_URL = "https://httpbin.com";
    private static final Map<String, String> PATHS = Map.of(
            "get", "/get",
            "slide-show", "/json");

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RestClientConfiguration restClientConfiguration;

    private HttpBinRestGateway gateway;

    @BeforeEach
    void beforeEach () {
        when(restClientConfiguration.getHttpBin()).thenReturn(buildHttpBinRestClientProperties());
        gateway = new HttpBinRestGateway(restTemplate, restClientConfiguration);
    }

    @ParameterizedTest
    @MethodSource(value = "getResponseEntityList")
    void get_returnsResponseEntity_whenRemoteServiceEndpointResponds (final ResponseEntity<String> expectedResponseEntity) {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null), eq(String.class)))
                .thenReturn(expectedResponseEntity);

        final ResponseEntity<String> responseEntity = gateway.get();

        assertEquals(responseEntity.getStatusCode(), expectedResponseEntity.getStatusCode());
        assertEquals(responseEntity.getBody(), expectedResponseEntity.getBody());
    }

    private static List<ResponseEntity<String>> getResponseEntityList() {
        return List.of(
                ok("anyBody"),
                notFound().build(),
                badRequest().body("badRequest"),
                ResponseEntity.internalServerError().build());
    }

    private RestClientConfiguration.Properties buildHttpBinRestClientProperties () {
        final RestClientConfiguration.Properties properties = new RestClientConfiguration.Properties();
        properties.setBaseUrl(BASE_URL);
        properties.setPaths(PATHS);
        return properties;
    }
}