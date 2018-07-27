package com.nexon.cloud;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

public class PreGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {

    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(config.getName(), config.getValue())
                    .build();

            HttpHeaders headers = request.getHeaders();
            List<String> authorization = headers.get("Authorization");
            String token = authorization.get(0);

            //Some handling code using oauth token....

            return chain.filter(exchange.mutate().request(request).build());
        };
    }


}
