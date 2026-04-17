package com.rh.customers.application.service.dto;

import lombok.Data;

@Data
public class CreatePersonDTO {
    private Long id;
    private String name;
    private String identificationNumber;
    private String gender;
    private String address;
    private String phone;
}
