package dev.coding.gateway.square.data;

import lombok.Data;

@Data
public class Payment {
    private String id;
    private String status;
    private String createdAt;
    private String sourceType;
    private String orderId;
    private String receiptNumber;
    private String receiptUrl;
    private CardDetails cardDetails;
    private Money amountMoney;
    private Money approvedMoney;
    private Money totalMoney;
}