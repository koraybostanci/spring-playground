package dev.coding.gateway;

import dev.coding.config.ServiceConfiguration.ServiceProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Slf4j
@AllArgsConstructor
public class RestGateway {

    private static final String REST_CALL_STARTING = "Rest call starting. URI: [{}]";
    private static final String REST_CALL_RESPONDED = "Rest call responded. URI: [{}], HttpStatus: [{}]";
    private static final String REST_CALL_FAILED = "Rest call failed. URI: [%s], HttpStatus: [%s], Reason: [%s]";

    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;

    protected <T> ResponseEntity<T> get(final URI uri, final Class<T> type) {
        ResponseEntity<T> responseEntity;
        try {
            log.debug(REST_CALL_STARTING, uri);
            responseEntity = restTemplate.exchange(buildRequestEntity(uri), type);
            log.info(REST_CALL_RESPONDED, uri, responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException ex) {
            log.error(format(REST_CALL_FAILED, uri, ex.getStatusCode(), ex.getMessage()));
            throw ex;
        }
        return responseEntity;
    }

    URI buildUriForPath (final String pathKey) {
        return fromUriString(serviceProperties.getBaseUrl())
                .path(serviceProperties.getPath(pathKey))
                .build().toUri();
    }

    private RequestEntity buildRequestEntity (final URI uri) {
        return new RequestEntity(buildHttpHeaders(), GET, uri);
    }

    private HttpHeaders buildHttpHeaders () {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return headers;
    }
}
