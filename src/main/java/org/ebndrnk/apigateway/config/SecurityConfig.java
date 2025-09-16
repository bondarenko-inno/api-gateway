package org.ebndrnk.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.ebndrnk.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for setting up reactive Spring Security.
 * Enables stateless security with JWT authentication, disables CSRF, and configures Swagger to be publicly accessible.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;


    /**
     * Defines the reactive security filter chain for the application.
     * <ul>
     *     <li>Disables CSRF protection (as we use JWT and stateless sessions).</li>
     *     <li>Allows unauthenticated access to whitelisted endpoints.</li>
     *     <li>Secures all other endpoints by requiring authentication.</li>
     *     <li>Adds a custom reactive JWT authentication filter.</li>
     * </ul>
     *
     * @param http the {@link ServerHttpSecurity} to modify
     * @return the configured {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }




}