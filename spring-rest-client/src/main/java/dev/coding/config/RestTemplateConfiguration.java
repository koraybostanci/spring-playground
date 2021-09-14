package dev.coding.config;

import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.apache.http.impl.client.HttpClientBuilder.create;

@Configuration
public class RestTemplateConfiguration {

    @Value("${rest-config.numberOfRoutes:1}")
    private int numberOfRoutes;

    @Value("${rest-config.maxConnectionsPerRoute:100}")
    private int maxConnectionsPerRoute;

    @Value("${rest-config.connectionRequestTimeout:1000}")
    private int connectionRequestTimeout;

    @Value("${rest-config.connectTimeout:1000}")
    private int connectTimeout;

    @Value("${rest-config.socketTimeout:1000}")
    private int socketTimeout;

    @Bean
    RestTemplate defaultRestTemplate(final RestTemplateBuilder builder) {
        builder.requestFactory(() -> clientHttpRequestFactory());
        return builder.build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        return buildHttpRequestFactory(buildRequestConfig());
    }

    private RequestConfig buildRequestConfig () {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
    }

    private HttpComponentsClientHttpRequestFactory buildHttpRequestFactory (final RequestConfig requestConfig) {
        return new HttpComponentsClientHttpRequestFactory(create()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(maxConnectionsPerRoute)
                .setMaxConnTotal(maxConnectionsPerRoute * numberOfRoutes)
                .build());
    }
}
