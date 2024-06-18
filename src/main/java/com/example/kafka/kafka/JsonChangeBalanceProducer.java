package com.example.kafka.kafka;

import com.example.kafka.dto.OperationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceProducer {

    private KafkaTemplate<Long, OperationDto> kafkaTemplate;


    public void send(LinkedList<OperationDto> operations) {
        log.info("Sending message to change balance to the topic ...");

        for (OperationDto operationDto : operations) {
            kafkaTemplate.send("change-balance", operationDto.accId(), operationDto);
        }
    }

}
