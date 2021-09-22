package dev.coding.service;

import dev.coding.gateway.HttpBinRestGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class HttpBinServiceImplIT {

    private static final String HTTP_BIN_CACHE_NAME = "http-bin-cache";

    @SpyBean
    private HttpBinRestGateway restGateway;

    @Autowired
    private HttpBinService service;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void beforeEach () {
        cacheManager.getCache(HTTP_BIN_CACHE_NAME).invalidate();
    }

    @Test
    void get_returnsResultFromCache_onConsecutiveCallsWithinCacheInterval() {
        for (int i = 0; i < 3; i++) {
            assertNotNull(service.get());
        }

        verify(restGateway).get();
    }

    @Test
    void get_returnsResultByCallingGateway_onConsecutiveCallsWhenCacheExpires() throws InterruptedException {
        service.get();
        Thread.sleep(SECONDS.toMillis(5));
        final String result = service.get();

        assertNotNull(result);
        verify(restGateway, times(2)).get();
    }
}