package dev.coding.domain;

import lombok.Data;

@Data
public class PaymentEntity {
    private String id;
    private String status;
    private String orderId;
}
