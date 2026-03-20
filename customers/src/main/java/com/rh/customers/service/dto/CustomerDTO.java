package com.rh.customers.service.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String password;
    private Boolean state;

    private PersonDTO person;

}
