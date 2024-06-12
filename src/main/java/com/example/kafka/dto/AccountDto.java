package com.example.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDto(
        Long id,
        @NotNull
        BigDecimal balance,
        @NotBlank
        Long personId
) {
}
