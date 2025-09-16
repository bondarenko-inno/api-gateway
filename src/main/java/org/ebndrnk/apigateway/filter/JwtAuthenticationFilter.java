package org.ebndrnk.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.apigateway.exception.ErrorInfo;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenValidator jwtTokenValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/actuator/health"
    );

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        log.info("Incoming request: {} {}", method, path);
        log.debug("Authorization header: {}", authHeader);

        if (HttpMethod.OPTIONS.equals(method) || isPublicPath(path)) {
            log.info("Skipping JWT validation for path: {}", path);
            return chain.filter(exchange);
        }


        // JWT проверка
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("JWT token missing or invalid for request: {} {}", method, path);
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "JWT token is missing or invalid!");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtTokenValidator.validateToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            log.info("JWT valid. User: {}, Role: {}", email, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );

            SecurityContext securityContext = new SecurityContextImpl(authentication);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        } catch (JwtException e) {
            log.error("JWT validation failed for request {} {}: {}", method, path, e.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT token!");
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter for request {} {}: {}", method, path, e.getMessage(), e);
            return writeError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "Authorization,Content-Type");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");


        ErrorInfo errorInfo = new ErrorInfo(
                LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorInfo);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("Failed to write error response: {}", e.getMessage(), e);
            return exchange.getResponse().setComplete();
        }
    }
}
