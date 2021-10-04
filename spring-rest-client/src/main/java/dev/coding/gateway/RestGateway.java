package dev.coding.gateway;

import dev.coding.common.exception.system.rest.RestCallFailedException;
import dev.coding.common.exception.system.rest.RestCallShouldRetryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.EMPTY_MAP;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;

@Slf4j
@RequiredArgsConstructor
public class RestGateway {

    private static final String REST_CALL_STARTING = "Rest call starting. URI: [{}]";
    private static final String REST_CALL_RESPONDED = "Rest call responded. URI: [{}], HttpStatus: [{}]";
    private static final String REST_CALL_FAILED = "Rest call failed. URI: [%s], HttpStatus: [%s], Reason: [%s]";
    private static final Set<HttpStatus> HTTP_STATUS_CODES_TO_RETRY = Set.of(REQUEST_TIMEOUT, TOO_MANY_REQUESTS);

    private final RestTemplate restTemplate;

    protected <T> ResponseEntity<T> get(final URI uri, final Class<T> type) {
        return get(uri, EMPTY_MAP, type);
    }

    protected <T> ResponseEntity<T> get(final URI uri, final Map<String, String> customHeaders, final Class<T> type) {
        final RequestEntity requestEntity = new RequestEntity(httpHeadersOf(customHeaders), GET, uri);
        return doHttpCall(requestEntity, type);
    }

    protected <T> ResponseEntity<T> post(final URI uri, final Object body, final Class<T> type) {
        final RequestEntity requestEntity = new RequestEntity(body, httpHeadersOf(EMPTY_MAP), POST, uri);
        return doHttpCall(requestEntity, type);
    }

    protected <T> ResponseEntity<T> put(final URI uri, final Object body, final Class<T> type) {
        final RequestEntity requestEntity = new RequestEntity(body, httpHeadersOf(EMPTY_MAP), PUT, uri);
        return doHttpCall(requestEntity, type);
    }

    private <T> ResponseEntity<T> doHttpCall (final RequestEntity requestEntity, final Class<T> type) {
        ResponseEntity<T> responseEntity;
        try {
            log.debug(REST_CALL_STARTING, requestEntity.getUrl());
            responseEntity = restTemplate.exchange(requestEntity, type);
            log.info(REST_CALL_RESPONDED, requestEntity.getUrl(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException ex) {
            return onHttpStatusCodeException(ex, requestEntity.getUrl());
        }
        return responseEntity;
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

    private HttpHeaders httpHeadersOf (final Map<String, String> customHeaders) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        if (customHeaders != null && !customHeaders.isEmpty()) {
            customHeaders.entrySet().stream()
                    .forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
        }

        return headers;
    }
}
