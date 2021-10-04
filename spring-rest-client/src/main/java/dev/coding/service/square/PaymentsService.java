package dev.coding.service.square;

import dev.coding.domain.PaymentEntity;

public interface PaymentsService {
    PaymentEntity getPayment(String paymentId);
}
