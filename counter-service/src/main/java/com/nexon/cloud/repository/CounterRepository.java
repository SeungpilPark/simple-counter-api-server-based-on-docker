package com.nexon.cloud.repository;

import com.nexon.cloud.model.Counter;
import org.reactivestreams.Publisher;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CounterRepository extends ReactiveCrudRepository<Counter, String> {

    Mono<Counter> findByUuid(String uuid);
}
