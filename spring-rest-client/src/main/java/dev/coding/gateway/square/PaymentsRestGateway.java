package dev.coding.gateway.square;

import dev.coding.config.ServiceConfiguration;
import dev.coding.config.ServiceConfiguration.ServiceProperties;
import dev.coding.gateway.RestGateway;
import dev.coding.gateway.square.data.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class PaymentsRestGateway extends RestGateway {

    private static final String GET_PAYMENT_PATH_KEY = "get-payment";

    private final ServiceProperties serviceProperties;

    PaymentsRestGateway (final RestTemplate restTemplate, final ServiceConfiguration serviceConfiguration) {
        super(restTemplate);
        serviceProperties = serviceConfiguration.getPayments();
    }

    public Optional<PaymentResponse> getPayment(final String paymentId) {
        final URI uri = buildUriComponentsBuilderOf(GET_PAYMENT_PATH_KEY)
                .buildAndExpand(paymentId)
                .toUri();

        final ResponseEntity<PaymentResponse> responseEntity =  get(uri, customHeaders(), PaymentResponse.class);
        return ofNullable(responseEntity.getBody());
    }

    private UriComponentsBuilder buildUriComponentsBuilderOf (final String pathKey) {
        return UriComponentsBuilder
                .fromUriString(serviceProperties.getBaseUrl())
                .path(serviceProperties.getPath(pathKey));
    }

    private Map<String, String> customHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Square-Version", serviceProperties.getVersion());
        headers.put("Authorization", format("Bearer %s", serviceProperties.getAccessToken()));
       return headers;
    }
}
