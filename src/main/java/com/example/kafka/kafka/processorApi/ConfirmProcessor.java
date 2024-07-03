package com.example.kafka.kafka.processorApi;

import com.example.kafka.entity.Operation;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import static com.example.kafka.config.KafkaTopicConfig.CONFIRMATION;


@Slf4j
@NoArgsConstructor
public class ConfirmProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        log.info("Consumed from topic {} Confirmed operation id: {}", CONFIRMATION, record.key());
        context.forward(record);
        context.commit();
    }
}

