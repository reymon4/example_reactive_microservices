package com.rh.customers.application.service.dto;

import lombok.Data;

@Data
public class CreateCustomerDTO {
    private Long id;
    private String password;
    private Boolean state;
    private CreatePersonDTO person;

}
