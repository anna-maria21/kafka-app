package com.example.kafka.entity;

import com.example.kafka.dto.OperType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.SecureRandomParameters;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@RedisHash("Operation")
public class Operation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;

    private OperType operationType;
    private BigDecimal amount;

    private Boolean isConfirmed = false;

}
