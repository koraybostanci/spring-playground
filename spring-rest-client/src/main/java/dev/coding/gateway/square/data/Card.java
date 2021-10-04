package dev.coding.gateway.square.data;

import lombok.Data;

@Data
public class Card {
    private String cardBrand;
    private String cardType;
    private String prepaidType;
    private int expYear;
    private int expMonth;
}