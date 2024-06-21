package com.example.kafka.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    private Long personId;

//    @ManyToOne
//    @JoinColumn(name = "personId")
//    private Person person;
//
//    @OneToMany(mappedBy = "id", fetch = FetchType.EAGER)
//    private Set<Operation> operations;
}
