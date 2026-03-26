package com.rh.customers.repository.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Table(name="person")
@Entity
public class PersonEntity {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name="address")
    private String address;

    @Column(name="phone")
    private String phone;

    @Column(name="identification_number")
    private String identificationNumber;

    @OneToOne(mappedBy = "person")
    private CustomerEntity customer;
}
