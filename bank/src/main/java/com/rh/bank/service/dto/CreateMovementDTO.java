package com.rh.bank.service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMovementDTO {

    private Long id;
    private String type;
    private BigDecimal value;
    private Boolean state;
    private String keyMovement;
    private String accountNumber;


}
