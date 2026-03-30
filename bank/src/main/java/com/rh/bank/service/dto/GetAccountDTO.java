package com.rh.bank.service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetAccountDTO {

    private Long id;
    private String number;
    private String type;
    private String customerName;
    private Boolean state;
    private BigDecimal initialBalance;

}
