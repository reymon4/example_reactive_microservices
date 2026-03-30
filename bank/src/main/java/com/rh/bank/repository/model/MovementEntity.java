package com.rh.bank.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movement",
        uniqueConstraints =
                {@UniqueConstraint(name = "uk_movement_key",
                        columnNames = "key_movement")})
@Data
public class MovementEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "type")
    private String type;

    @Column(name = "initial_balance")
    private BigDecimal initialBalance;

    @Column(name = "available_balance")
    private BigDecimal availableBalance;

    @Column(name="date")
    private LocalDateTime date;

    @Column(name="key_movement")
    private String keyMovement;


    @Column(name="state")
    private Boolean state;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "account_id")
    private AccountEntity account;
}
