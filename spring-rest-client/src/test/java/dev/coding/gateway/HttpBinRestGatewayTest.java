package dev.coding.gateway;

import dev.coding.config.ServiceConfiguration;
import dev.coding.config.ServiceConfiguration.ServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.internalServerError;
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
    private ServiceConfiguration serviceConfiguration;

    private HttpBinRestGateway gateway;

    @BeforeEach
    void beforeEach () {
        when(serviceConfiguration.getHttpBin()).thenReturn(buildHttpBinServiceProperties());
        gateway = new HttpBinRestGateway(restTemplate, serviceConfiguration);
    }

    @ParameterizedTest
    @MethodSource(value = "getResponseEntityList")
    void get_returnsResponseEntity_whenRemoteServiceEndpointResponds (final ResponseEntity<String> expectedResponseEntity) {
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class)))
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
                internalServerError().build());
    }

    private ServiceProperties buildHttpBinServiceProperties () {
        final ServiceProperties properties = new ServiceProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setPaths(PATHS);
        return properties;
    }
}