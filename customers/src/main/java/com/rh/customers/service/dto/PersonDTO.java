package com.rh.customers.service.dto;

import lombok.Data;

@Data
public class PersonDTO {
    private Long id;
    private String name;
    private String identificationNumber;
    private String gender;
    private String address;
    private String phone;
}
