package com.rh.customers.event.producer;

import lombok.Data;


//Object to create, update and delete customer

public record CustomerEvent(String identificationNumber, String name, Boolean state) {
}
