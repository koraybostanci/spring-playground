package dev.coding.gateway;

import dev.coding.config.RestClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Component
@Slf4j
public class HttpBinRestGateway {

    private static final String GET_PATH_KEY = "get";

    private final RestTemplate restTemplate;
    private final RestClientConfiguration.Properties properties;

    HttpBinRestGateway (final RestTemplate restTemplate,
                        final RestClientConfiguration restClientConfiguration) {
        this.restTemplate = restTemplate;
        this.properties = restClientConfiguration.getHttpBin();
    }

    public ResponseEntity<String> get() {
        final URI uri = fromUriString(properties.getBaseUrl())
                .path(properties.getPath(GET_PATH_KEY))
                .build().toUri();
        return restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
    }
}
