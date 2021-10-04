package dev.coding.service.square;

import dev.coding.common.exception.business.EntityNotFoundException;
import dev.coding.domain.PaymentEntity;
import dev.coding.gateway.square.PaymentsRestGateway;
import dev.coding.gateway.square.data.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentsRestGateway paymentsRestGateway;

    @Override
    public PaymentEntity getPayment (final String paymentId) {
        final Optional<PaymentResponse> paymentResponse = paymentsRestGateway.getPayment(paymentId);
        if (paymentResponse.isEmpty()) {
            throw new EntityNotFoundException(format("PaymentEntity with Id=[%s]", paymentId));
        }
        return toPaymentEntity(paymentResponse.get());
    }

    private PaymentEntity toPaymentEntity (final PaymentResponse paymentResponse) {
        final PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentResponse.getPayment().getId());
        paymentEntity.setStatus(paymentResponse.getPayment().getStatus());
        paymentEntity.setOrderId(paymentResponse.getPayment().getOrderId());
        return paymentEntity;
    }
}
