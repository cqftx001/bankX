package com.bankx.demo.transaction.dto;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class ReverseTransactionRequest{

    private final String reason;

    public String reason(){
        return reason;
    }
}