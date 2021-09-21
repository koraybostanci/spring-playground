package dev.coding.gateway;

import dev.coding.common.exception.system.rest.RestCallFailedException;
import dev.coding.common.exception.system.rest.RestCallShouldRetryException;
import dev.coding.config.ServiceConfiguration.ServiceProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Set;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Slf4j
@AllArgsConstructor
public class RestGateway {

    private static final String REST_CALL_STARTING = "Rest call starting. URI: [{}]";
    private static final String REST_CALL_RESPONDED = "Rest call responded. URI: [{}], HttpStatus: [{}]";
    private static final String REST_CALL_FAILED = "Rest call failed. URI: [%s], HttpStatus: [%s], Reason: [%s]";
    private static final Set<HttpStatus> HTTP_STATUS_CODES_TO_RETRY = Set.of(REQUEST_TIMEOUT, TOO_MANY_REQUESTS);

    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;

    protected <T> ResponseEntity<T> get(final URI uri, final Class<T> type) {
        ResponseEntity<T> responseEntity;
        try {
            log.debug(REST_CALL_STARTING, uri);
            responseEntity = restTemplate.exchange(buildRequestEntity(uri), type);
            log.info(REST_CALL_RESPONDED, uri, responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException ex) {
            return onHttpStatusCodeException(ex, uri);
        }
        return responseEntity;
    }

    URI buildUriForPath (final String pathKey) {
        return fromUriString(serviceProperties.getBaseUrl())
                .path(serviceProperties.getPath(pathKey))
                .build().toUri();
    }

    private <T> ResponseEntity<T> onHttpStatusCodeException (final HttpStatusCodeException ex, final URI uri) {
        final HttpStatus statusCode = ex.getStatusCode();
        if (statusCode == NOT_FOUND) {
            return notFound().build();
        }

        final boolean shouldRetry = statusCode.is5xxServerError() || HTTP_STATUS_CODES_TO_RETRY.contains(statusCode);
        if (shouldRetry) {
            throw new RestCallShouldRetryException(format(REST_CALL_FAILED, uri, statusCode, ex.getMessage()), ex);
        }

        throw new RestCallFailedException(ex);
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
