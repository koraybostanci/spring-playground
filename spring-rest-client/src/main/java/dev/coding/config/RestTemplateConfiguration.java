package dev.coding.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.apache.http.impl.client.HttpClientBuilder.create;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@RequiredArgsConstructor
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

    private final ObjectMapper objectMapper;

    @Bean
    public RestTemplate defaultRestTemplate (final RestTemplateBuilder builder) {
        final RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        restTemplate.getMessageConverters().add(0, getMappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    private HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(APPLICATION_JSON,APPLICATION_OCTET_STREAM));
        converter.setObjectMapper(objectMapper);
        return converter;
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
