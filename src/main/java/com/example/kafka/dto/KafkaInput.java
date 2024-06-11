package com.example.kafka.dto;

import lombok.*;

import java.util.LinkedList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class KafkaInput {
    private Long accId;
    private LinkedList<Operation> operations;
}
