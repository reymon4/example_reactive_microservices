package com.rh.bank.service.dto;

import lombok.Data;

@Data
public class GetMovementDTO {
    private Long id;
    private String detail;
    private Boolean state;
    private Boolean initialBalance;
    private String accountType;
    private String accountNumber;
}
