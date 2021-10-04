package dev.coding.gateway.square;

import dev.coding.config.ServiceConfiguration;
import dev.coding.config.ServiceConfiguration.ServiceProperties;
import dev.coding.gateway.square.data.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static dev.coding.TestObjectFactory.anyPaymentResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;

@ExtendWith(MockitoExtension.class)
public class PaymentsRestGatewayTest {

    private static final String BASE_URL = "https://square-payments-api.com";
    private static final Map<String, String> PATHS = Map.of(
            "get-payment", "/payments"
    );

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ServiceConfiguration serviceConfiguration;

    private PaymentsRestGateway gateway;

    @BeforeEach
    void beforeEach () {
        when(serviceConfiguration.getPayments()).thenReturn(buildPaymentsProperties());
        gateway = new PaymentsRestGateway(restTemplate, serviceConfiguration);
    }

    @Test
    void getPayment_returnsPaymentResponse_onHttpOk () {
        final String paymentId = "anyPaymentId";
        final PaymentResponse expectedPaymentResponse = anyPaymentResponse(paymentId);

        when(restTemplate.exchange(any(RequestEntity.class), eq(PaymentResponse.class)))
                .thenReturn(ok(expectedPaymentResponse));

        final Optional<PaymentResponse> paymentResponse = gateway.getPayment(paymentId);

        assertEquals(expectedPaymentResponse, paymentResponse.get());
    }

    private ServiceProperties buildPaymentsProperties () {
        final ServiceProperties properties = new ServiceProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setPaths(PATHS);
        return properties;
    }
}