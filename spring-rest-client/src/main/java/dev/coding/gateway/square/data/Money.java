package dev.coding.gateway.square.data;

import lombok.Data;

@Data
public class Money {
    private int amount;
    private String currency;
}