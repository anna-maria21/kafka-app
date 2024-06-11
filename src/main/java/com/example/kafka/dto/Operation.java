package com.example.kafka.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Operation {
    private OperType operType;
    private BigDecimal amount;
}
