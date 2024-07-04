package com.example.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TestAppender extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> logEvents = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        logEvents.add(eventObject);
    }

}
