package com.example.kafka.entity;

import com.example.kafka.dto.OperType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Builder
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OperType operType;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "accId")
    private Account account;
}
