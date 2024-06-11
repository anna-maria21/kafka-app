package com.example.kafka.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class InputArrayItem {
    private OperType operType;
    private BigDecimal amount;
}
