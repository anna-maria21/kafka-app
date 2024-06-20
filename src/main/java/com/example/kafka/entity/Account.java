package com.example.kafka.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "personId")
    private Person person;

    @OneToMany(mappedBy = "id", fetch = FetchType.EAGER)
    private Set<Operation> operations;
}
