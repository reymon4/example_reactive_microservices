package com.rh.bank.infrastructure.event.producer;


//Object to create, update and delete customer

import java.time.LocalDateTime;

public record AccountEvent(String accountNumber, String type, LocalDateTime timestamp, String identificationNumberCustomer) {
}
