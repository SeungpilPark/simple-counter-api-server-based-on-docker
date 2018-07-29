package com.nexon.cloud.scheduler;

import com.nexon.cloud.kafka.CounterMessage;
import com.nexon.cloud.kafka.CounterProcessor;
import com.nexon.cloud.redis.LeaderWrapper;
import com.nexon.cloud.repository.CounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;

/**
 * Created by sppark on 2017. 12. 4..
 */
@Component
public class CounterScheduler {

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private CounterProcessor counterProcessor;

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterScheduler.class);

    @Autowired
    private LeaderWrapper leaderWrapper;

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void leaderScheduler() {
        //if not leader, skip.
        if (!leaderWrapper.amILeader()) {
            return;
        }
        try {
            this.processTimerJob();
        } catch (Exception ex) {

        }
    }

    @Async
    public void processTimerJob() {
        try {
            LOGGER.info("leader processTimerJob start");

            long currentTime = new Date().getTime();
            counterRepository.findAll()
                    .flatMap(counter -> {
                        CounterMessage counterMessage = new CounterMessage();
                        long diff = (currentTime - counter.getCreatedDate().getTime()) / 1000;
                        counter.setCurrent(diff);

                        if (counter.getTo() <= diff) {
                            LOGGER.info("Kafka Removal Message Throw, {}", counter.getUuid());
                            counterMessage.setMethod("DELETE");
                            counterMessage.setCounter(counter);
                            counterProcessor.sendCounterMessage(counterMessage);
                        } else {
                            counterMessage.setMethod("PUT");
                            counterMessage.setCounter(counter);
                            counterProcessor.sendCounterMessage(counterMessage);
                        }
                        return Mono.just(counter);
                    })
                    .subscribeOn(Schedulers.elastic())
                    .subscribe();
        } catch (Exception ex) {
            LOGGER.error("leader processTimerJob failed.");
        }
    }
}
