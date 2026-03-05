package com.innowise.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

  private static final String API_PREFIX = "/api/v1";

  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("auth-service", p -> p.
            path("/login/**", "/validate/**", "/refresh/**", "/register/**")
            .filters(f -> f.prefixPath(API_PREFIX+ "/auth"))
            .uri("http://localhost:8082"))

        .route("order-service", p -> p.
            path("/orders/**", "/items/**", "/users/orders/**")
            .filters(f -> f.prefixPath(API_PREFIX))
            .uri("http://localhost:8081"))

        .route("user-service", p -> p.
            path("/users/**", "/cards/**").
            filters(f -> f.prefixPath(API_PREFIX))
            .uri("http://localhost:8080")).build();
  }
}
