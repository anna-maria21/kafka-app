package com.example.kafka.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record OperationDto (
        Long id,

        @NotBlank(message = "Choose operation type (REFUND or WITHDRAWAL)")
        OperType operType,
        @NotBlank(message = "Enter sum of operation")
        @DecimalMin(value = "0.0")
        @Digits(integer = 20, fraction = 3)
        BigDecimal amount,
        Long accId
        ) {}