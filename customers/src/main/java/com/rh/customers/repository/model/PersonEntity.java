package com.rh.customers.repository.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name="person")
public class PersonEntity {

    @Id
    private Long id;

    private String name;

    private String gender;

    private String address;

    private String phone;

    private String identificationNumber;
}
