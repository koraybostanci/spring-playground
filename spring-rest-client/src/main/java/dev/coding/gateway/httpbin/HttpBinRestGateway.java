package dev.coding.gateway.httpbin;

import dev.coding.config.ServiceConfiguration;
import dev.coding.config.ServiceConfiguration.ServiceProperties;
import dev.coding.gateway.RestGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Component
@Slf4j
public class HttpBinRestGateway extends RestGateway {

    private static final String GET_PATH_KEY = "get";
    private static final String POST_PATH_KEY = "post";
    private static final String PUT_PATH_KEY = "put";
    private static final String RESILIENCY_CONFIG_NAME = "http-bin";

    private final ServiceProperties serviceProperties;

    HttpBinRestGateway (final RestTemplate restTemplate, final ServiceConfiguration serviceConfiguration) {
        super(restTemplate);
        serviceProperties = serviceConfiguration.getHttpBin();
    }

    @Retry(name = RESILIENCY_CONFIG_NAME)
    @CircuitBreaker(name = RESILIENCY_CONFIG_NAME)
    public ResponseEntity<String> get() {
        return get(buildUriForPath(GET_PATH_KEY), String.class);
    }

    @Retry(name = RESILIENCY_CONFIG_NAME)
    @CircuitBreaker(name = RESILIENCY_CONFIG_NAME)
    public ResponseEntity<String> post(final Object data) {
        return post(buildUriForPath(POST_PATH_KEY), data, String.class);
    }

    @Retry(name = RESILIENCY_CONFIG_NAME)
    @CircuitBreaker(name = RESILIENCY_CONFIG_NAME)
    public ResponseEntity<String> put(final Object data) {
        return put(buildUriForPath(PUT_PATH_KEY), data, String.class);
    }

    private URI buildUriForPath(final String pathKey) {
        return fromUriString(serviceProperties.getBaseUrl())
                .path(serviceProperties.getPath(pathKey))
                .build().toUri();
    }
}
