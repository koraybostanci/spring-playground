package dev.coding.service.square;

import dev.coding.common.exception.business.EntityNotFoundException;
import dev.coding.domain.PaymentEntity;
import dev.coding.gateway.square.PaymentsRestGateway;
import dev.coding.gateway.square.data.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.coding.TestObjectFactory.anyPaymentResponse;
import static dev.coding.TestObjectFactory.paymentEntityOf;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentsServiceImplTest {

    @Mock
    private PaymentsRestGateway paymentsRestGateway;

    @InjectMocks
    private PaymentsServiceImpl paymentsService;

    @Test
    void getPayment_throwsEntityNotFoundException_whenNoPaymentResponseFoundByGivenId () {
        final String paymentId = "anyPaymentId";
        when(paymentsRestGateway.getPayment(paymentId)).thenReturn(empty());

        final Exception exception = assertThrows(EntityNotFoundException.class, () -> paymentsService.getPayment(paymentId));

        assertEquals(exception.getMessage(), format("PaymentEntity with Id=[%s]", paymentId));
        verify(paymentsRestGateway).getPayment(paymentId);
    }

    @Test
    void getPayment_returnsPaymentEntity_whenPaymentResponseFoundByGivenId () {
        final String paymentId = "anyPaymentId";
        final PaymentResponse expectedPaymentResponse = anyPaymentResponse(paymentId);
        final PaymentEntity expectedPaymentEntity = paymentEntityOf(expectedPaymentResponse);
        when(paymentsRestGateway.getPayment(paymentId)).thenReturn(of(expectedPaymentResponse));

        final PaymentEntity paymentEntity = paymentsService.getPayment(paymentId);

        assertEquals(paymentEntity, expectedPaymentEntity);
        verify(paymentsRestGateway).getPayment(paymentId);
    }
}