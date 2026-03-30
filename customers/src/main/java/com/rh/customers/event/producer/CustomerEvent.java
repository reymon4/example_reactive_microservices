package com.rh.customers.event.producer;

import lombok.Data;


//Object to create, update and delete customer
@Data
public class CustomerEvent {
    private String identificationNumber;
    private String name;
    private Boolean state;
}
