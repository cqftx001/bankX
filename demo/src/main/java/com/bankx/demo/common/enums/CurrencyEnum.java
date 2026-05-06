package com.bankx.demo.common.enums;

public enum CurrencyEnum {
    USD("USD"),
    EUR("EUR"),
    JPY("JPY"),
    AUD("AUD"),
    CAD("CAD"),
    CNY("CNY");

    private String currency;

    CurrencyEnum(String currency) {
        this.currency = currency;
    }
}
