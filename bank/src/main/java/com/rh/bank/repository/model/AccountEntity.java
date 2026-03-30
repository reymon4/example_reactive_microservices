package com.rh.bank.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "account",
        uniqueConstraints =
                {@UniqueConstraint(name = "uk_account_number_person_identification_number",
                        columnNames = {"number", "person_identification_number"})})
public class AccountEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private String number;

    @Column(name = "type")
    private String type;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "person_identification_number")
    private String personIdentificationNumber;

    @Column(name = "state")
    private Boolean state;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<MovementEntity> movimientos;
}
