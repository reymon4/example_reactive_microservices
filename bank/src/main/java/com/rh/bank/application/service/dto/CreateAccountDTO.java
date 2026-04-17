package com.rh.bank.application.service.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountDTO {
    private Long id;
    private String number;
    private String type;
    private String personIdentificationNumber;
    private Boolean state;
    private BigDecimal initialBalance;
}
