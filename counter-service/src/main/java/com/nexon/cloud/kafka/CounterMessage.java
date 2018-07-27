package com.nexon.cloud.kafka;

import com.nexon.cloud.model.Counter;

public class CounterMessage {
    private Counter counter;

    private String method;

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
