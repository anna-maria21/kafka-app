package com.example.kafka.dto;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {
    private Long id;
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "personId", nullable = false)
    private Person person;


}
