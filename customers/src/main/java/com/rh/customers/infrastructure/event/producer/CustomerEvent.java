package com.rh.customers.infrastructure.event.producer;

import lombok.Data;

import java.time.LocalDateTime;


//Object to create, update and delete customer

public record CustomerEvent(String identificationNumber, String type, LocalDateTime timestamp) {
}
