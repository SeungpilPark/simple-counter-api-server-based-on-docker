package com.nexon.cloud;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CacheGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    CacheService cacheService;

    @Override
    public int getOrder() {
        return -2; // -1 is response write filter, must be called before that
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //cache key
        String requestUri = exchange.getRequest().getURI().toString();

        //All Get Method using cache logic.
        if (exchange.getRequest().getMethod().equals(HttpMethod.GET)) {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            //For certain cases, be sure registered backend servers is 0 in Eureka.
            List<ServiceInstance> instances = discoveryClient.getInstances("counter-service");
            if (instances.isEmpty()) {

                //load cache
                CacheEntity entity = cacheService.load(requestUri);
                if (entity != null) {
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse);

                    //fill body
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(entity.getContent().getBytes(Charset.forName("UTF-8")));

                    //fill headers
                    for (Map.Entry<String, String> entry : entity.getHeaders().entrySet()) {
                        decoratedResponse.getHeaders().add(entry.getKey(), entry.getValue());
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.OK);

                    //return stream
                    return decoratedResponse.writeWith(Flux.just(buffer));
                } else {
                    return chain.filter(exchange);
                }
            } else {
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                        //if 200 status, save cache
                        if (this.getStatusCode().equals(HttpStatus.OK)) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                                return super.writeWith(fluxBody.map(dataBuffer -> {
                                    // probably should reuse buffers
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);

                                    saveCache(requestUri, content, this.getHeaders().toSingleValueMap());
                                    //cacheService.save(requestUri, content, this.getHeaders().toSingleValueMap());

                                    //return non blocking stream
                                    return bufferFactory.wrap(content);
                                }));
                            }
                            return super.writeWith(body); // if body is not a flux. never got there.
                        } else {
                            return super.writeWith(body);
                        }
                    }
                };
                //return chain.filter(exchange);
                return chain.filter(exchange.mutate().response(decoratedResponse).build()); // replace response with decorator
            }
        } else {

            //Normal response
            return chain.filter(exchange);
        }
    }

    @Async
    public void saveCache(String requestUri, byte[] content, Map headers) {
        cacheService.save(requestUri, content, headers);
    }

}
