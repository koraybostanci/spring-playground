package dev.coding.gateway.square;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.coding.common.exception.system.rest.RestCallFailedException;
import dev.coding.common.exception.system.rest.RestCallShouldRetryException;
import dev.coding.gateway.square.data.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static dev.coding.TestObjectFactory.anyPaymentResponse;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@WireMockTest(httpPort = 8001)
@SpringBootTest
public class PaymentsRestGatewayIT {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentsRestGateway gateway;
    @SpyBean
    private RestTemplate restTemplate;

    @BeforeEach
    void beforeEach () {
        WireMock.reset();
    }

    @Test
    void getPayment_returnsPaymentResponse_onHttpOk () throws JsonProcessingException {
        final String paymentId = "anyPaymentId";
        final PaymentResponse expectedPaymentResponse = anyPaymentResponse(paymentId);
        stubForGet("/payments/" + paymentId, ResponseEntity.ok(expectedPaymentResponse));

        final Optional<PaymentResponse> paymentResponse = gateway.getPayment(paymentId);

        assertEquals(expectedPaymentResponse, paymentResponse.get());
        verify(restTemplate).exchange(any(RequestEntity.class), eq(PaymentResponse.class));
    }

    @Test
    void getPayment_returnsEmpty_onHttpNotFound () throws JsonProcessingException {
        final String paymentId = "anyPaymentId";
        stubForGet("/payments/" + paymentId, ResponseEntity.notFound().build());

        final Optional<PaymentResponse> paymentResponse = gateway.getPayment(paymentId);

        assertEquals(empty(), paymentResponse);
        verify(restTemplate).exchange(any(RequestEntity.class), eq(PaymentResponse.class));
    }

    @Test
    void getPayment_throwsRestCallFailedException_onHttpBadRequest() throws JsonProcessingException {
        final String paymentId = "anyPaymentId";
        stubForGet("/payments/" + paymentId, ResponseEntity.badRequest().build());

        assertThrows(RestCallFailedException.class,  () -> gateway.getPayment(paymentId));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(PaymentResponse.class));
    }

    @Test
    void getPayment_throwsRestCallShouldRetryException_onHttp5xxServerError() throws JsonProcessingException {
        final String paymentId = "anyPaymentId";
        stubForGet("/payments/" + paymentId, ResponseEntity.status(BAD_GATEWAY).build());

        assertThrows(RestCallShouldRetryException.class,  () -> gateway.getPayment(paymentId));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(PaymentResponse.class));
    }

    @ParameterizedTest
    @MethodSource("getHttpStatusCodesToRetry")
    void getPayment_throwsRestCallShouldRetryException_onHttpStatusCodesToRetry(final HttpStatus httpStatus) throws JsonProcessingException {
        final String paymentId = "anyPaymentId";
        stubForGet("/payments/" + paymentId, ResponseEntity.status(httpStatus).build());

        assertThrows(RestCallShouldRetryException.class,  () -> gateway.getPayment(paymentId));

        verify(restTemplate).exchange(any(RequestEntity.class), eq(PaymentResponse.class));
    }

    private static Set<HttpStatus> getHttpStatusCodesToRetry() {
        return Set.of(REQUEST_TIMEOUT, TOO_MANY_REQUESTS);
    }

    private void stubForGet (final String url, final ResponseEntity<PaymentResponse> responseEntity) throws JsonProcessingException {
        stubForPath(WireMock.get(url), responseEntity);
    }

    private void stubForPath (final MappingBuilder mappingBuilder, final ResponseEntity<PaymentResponse> responseEntity) throws JsonProcessingException {
        final byte[] body = objectMapper.writeValueAsBytes(responseEntity.getBody());
        WireMock.stubFor(mappingBuilder
                .willReturn(responseDefinition()
                        .withStatus(responseEntity.getStatusCodeValue())
                        .withBody(body)));
    }
}
