package com.rh.bank.application.clients.dto;

import lombok.Data;

@Data
public class GetCustomerDTO {
    private Long id;
    private Boolean state;
    private String customerName;
    private String phone;
    private String address;
    private String identificationNumber;
}
