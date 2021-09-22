package dev.coding.service;

import dev.coding.gateway.HttpBinRestGateway;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class HttpBinServiceImpl implements HttpBinService {

    private static final String HTTP_BIN_CACHE_NAME = "http-bin-cache";
    private final HttpBinRestGateway httpBinRestGateway;

    @Cacheable(HTTP_BIN_CACHE_NAME)
    public String get () {
        final ResponseEntity<String> responseEntity = httpBinRestGateway.get();
        return responseEntity.getBody();
    }
}
