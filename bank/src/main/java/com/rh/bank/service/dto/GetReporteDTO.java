package com.rh.bank.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GetReporteDTO {
    private LocalDateTime date;
    private String customerName;
    private String accountNumber;
    private String accountType;
    private String movementType;
    private Boolean movementState;
    private BigDecimal movementInitialBalance;
    private BigDecimal movementValue;
    private BigDecimal movementBalance;


}
