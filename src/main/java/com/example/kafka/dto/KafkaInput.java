package com.example.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KafkaInput {
    private Long accId;
    private LinkedList<InputArrayItem> operations;
}
