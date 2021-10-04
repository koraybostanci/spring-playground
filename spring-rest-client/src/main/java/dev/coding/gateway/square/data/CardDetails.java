package dev.coding.gateway.square.data;

import lombok.Data;

@Data
public class CardDetails {
    private Card card;
    private String status;
    private String cvvStatus;
    private String avsStatus;
}