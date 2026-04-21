package com.bankx.demo.common.enums;

public enum CurrencyEnum {
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY"),
    AUD("AUD"),
    CAD("CAD"),
    CHF("CHF"),
    CNY("CNY"),
    SEK("SEK"),
    NZD("NZD"),
    NOK("NOK"),
    ZAR("ZAR"),
    RUB("RUB"),
    INR("INR"),
    BRL("BRL"),
    MXN("MXN");

    private String currency;

    CurrencyEnum(String currency) {
        this.currency = currency;
    }
}
