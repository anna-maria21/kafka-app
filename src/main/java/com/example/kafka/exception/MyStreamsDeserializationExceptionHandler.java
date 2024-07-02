package com.example.kafka.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

//@AllArgsConstructor
@Component
@Slf4j
public class MyStreamsDeserializationExceptionHandler implements DeserializationExceptionHandler {
    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    public MyStreamsDeserializationExceptionHandler(KafkaTemplate<Long, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    public DeserializationHandlerResponse handle(ProcessorContext context, ConsumerRecord<byte[], byte[]> record, Exception exception) {
        log.error("Error while processing record: ", exception);
        kafkaTemplate.send("my-retry", record.value());
        return DeserializationHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
