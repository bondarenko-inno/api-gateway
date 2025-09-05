package org.ebndrnk.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationLoggingFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER))
                .orElse(UUID.randomUUID().toString());

        ServerHttpRequest mutated = exchange.getRequest()
                .mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        var start = System.currentTimeMillis();
        log.info("IN  [{}] {} {}", correlationId, mutated.getMethod(), mutated.getURI());

        return chain.filter(exchange.mutate().request(mutated).build())
                .doOnSuccess(v -> log.info("OUT [{}] {} {} -> status={} took={}ms",
                        correlationId,
                        mutated.getMethod(),
                        mutated.getURI(),
                        exchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - start
                ))
                .doOnError(err -> log.error("ERR [{}] {} {} -> {} took={}ms",
                        correlationId,
                        mutated.getMethod(),
                        mutated.getURI(),
                        err.toString(),
                        System.currentTimeMillis() - start
                ));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
