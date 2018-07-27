package com.nexon.cloud.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexon.cloud.model.Counter;
import com.nexon.cloud.repository.CounterRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class CounterProcessor {


    @Autowired
    private CounterRepository counterRepository;

    private final Log logger = LogFactory.getLog(getClass());

    private CounterStreams counterStreams;

    public CounterProcessor(CounterStreams counterStreams) {
        this.counterStreams = counterStreams;
    }


    @Async
    public void sendCounterMessage(final CounterMessage counterMessage) {
        logger.info("Sending counterMessage : " + counterMessage.getMethod());

        MessageChannel messageChannel = counterStreams.counterSource();
        messageChannel.send(MessageBuilder
                .withPayload(counterMessage)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
    }

    @StreamListener
    public void receiveCounterMessage(@Input(CounterStreams.INPUT) Flux<String> inbound) {
        inbound
                .log()
                .subscribeOn(Schedulers.elastic())
                .subscribe(value -> {
                    try {
                        CounterMessage counterMessage = new ObjectMapper().readValue(value, CounterMessage.class);
                        Counter counter = counterMessage.getCounter();
                        String method = counterMessage.getMethod();

                        switch (method) {
                            case "POST": {
                                counterRepository.save(counter)
                                        .block();
                                break;
                            }

                            case "PUT": {
                                counterRepository.save(counter)
                                        .block();
                                break;
                            }

                            case "DELETE": {
                                counterRepository.delete(counter)
                                        .block();
                                break;
                            }
                        }

                    } catch (Exception ex) {
                        //throw new RuntimeException("Conversation failed");
                    }
                }, error -> System.err.println("CAUGHT " + error));

    }
}


