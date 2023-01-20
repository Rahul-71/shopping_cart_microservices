package com.microservice.cloudgateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2WebSecurity {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange()
                .anyExchange().authenticated()    // authenticate all our api
                .and()
                .oauth2Login()                  // also, provide basic oauth login page
                .and()
                .oauth2ResourceServer()        
                .jwt();                         // configures basic oauth2 jwt resource server

        return http.build();
    }

}
