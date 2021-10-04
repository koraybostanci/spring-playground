package dev.coding;

import dev.coding.domain.PaymentEntity;
import dev.coding.gateway.square.data.Card;
import dev.coding.gateway.square.data.CardDetails;
import dev.coding.gateway.square.data.Money;
import dev.coding.gateway.square.data.Payment;
import dev.coding.gateway.square.data.PaymentResponse;

public final class TestObjectFactory {

    public static PaymentEntity paymentEntityOf (final PaymentResponse paymentResponse) {
        final PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentResponse.getPayment().getId());
        paymentEntity.setStatus(paymentResponse.getPayment().getStatus());
        paymentEntity.setOrderId(paymentResponse.getPayment().getOrderId());
        return paymentEntity;
    }

    public static PaymentResponse anyPaymentResponse (final String paymentId) {
        final PaymentResponse response = new PaymentResponse();
        response.setPayment(anyResponse(paymentId));
        return response;
    }

    public static Payment anyResponse (final String paymentId) {
        final Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setCreatedAt("2021-10-03T18:27:25.257Z");
        payment.setOrderId("5s5hMFrbigkt9c3UG0WUxhawoh4F");
        payment.setReceiptUrl("https://squareupsandbox.com/receipt/preview/jBChG2gKZ3SsQQoHoAm8U491QDbZY");
        payment.setReceiptNumber("jBCh");
        payment.setSourceType("CARD");
        payment.setStatus("COMPLETED");
        payment.setCardDetails(anyCardDetails());
        payment.setAmountMoney(anyMoney(100, "USD"));
        payment.setApprovedMoney(anyMoney(100, "USD"));
        payment.setTotalMoney(anyMoney(100, "USD"));
        return payment;
    }

    private static CardDetails anyCardDetails () {
        final CardDetails cardDetails = new CardDetails();
        cardDetails.setStatus("CAPTURED");
        cardDetails.setAvsStatus("AVS_ACCEPTED");
        cardDetails.setCvvStatus("CVV_ACCEPTED");
        cardDetails.setCard(anyCard());
        return cardDetails;
    }

    private static Card anyCard () {
        final Card card = new Card();
        card.setCardType("CREDIT");
        card.setCardBrand("VISA");
        card.setPrepaidType("NOT_PREPAID");
        card.setExpYear(2023);
        card.setExpMonth(10);
        return card;
    }

    private static Money anyMoney (final int amount, final String currency) {
        final Money money = new Money();
        money.setAmount(amount);
        money.setCurrency(currency);
        return money;
    }
}
