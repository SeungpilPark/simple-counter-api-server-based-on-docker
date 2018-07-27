package com.nexon.cloud.controller;

import com.nexon.cloud.kafka.CounterMessage;
import com.nexon.cloud.kafka.CounterProcessor;
import com.nexon.cloud.model.Counter;
import com.nexon.cloud.repository.CounterRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/counter")
public class CounterController {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    CounterRepository counterRepository;

    @Autowired
    CounterProcessor counterProcessor;

    @GetMapping("")
    public Flux<String> listCounterUUIds() {
        Flux<Counter> all = counterRepository.findAll();
        return all
                .log()
                .map(counter -> counter.getUuid() + "\n");
    }

    @GetMapping("/counts-all")
    public Mono<Long> counts() {
        return counterRepository.count();
    }

    @PostMapping("")
    public String sendCounterCreateMessage(@RequestParam(required = true, value = "to") Long to) {
        Counter counter = new Counter();
        counter.setTo(to);
        counter.setUuid(UUID.randomUUID().toString());

        CounterMessage message = new CounterMessage();
        message.setMethod("POST");
        message.setCounter(counter);

        counterProcessor.sendCounterMessage(message);
        return counter.getUuid() + "\n";
    }

    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<Map>> getCounterSimpleFormat(@PathVariable(value = "uuid") String uuid) {
        Map map = new HashMap();
        return counterRepository.findByUuid(uuid)
                .log()
                .map(counter -> {
                    map.put("current", counter.getCurrent());
                    map.put("to", counter.getTo());
                    return new ResponseEntity<Map>(map, HttpStatus.OK);
                })
                .defaultIfEmpty(new ResponseEntity<>(new HashMap(), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{uuid}/stop")
    public ResponseEntity<String> deleteCustomer(@PathVariable("uuid") String uuid) {
        try {
            counterRepository.findByUuid(uuid)
                    .map(counter -> {
                        return counterRepository.delete(counter)
                                .subscribeOn(Schedulers.elastic())
                                .subscribe();
                    })
                    .subscribeOn(Schedulers.elastic())
                    .subscribe();
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.OK);
        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
