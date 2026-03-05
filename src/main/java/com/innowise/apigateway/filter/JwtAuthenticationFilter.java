package com.innowise.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private static final List<String> EXCLUDED_PATHS = List.of("/login", "/register");

  @Value("${jwt.secret}")
  private String secret;

  private SecretKey key;

  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      log.warn("Missing or invalid Authorization header for request: {}", path);
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);

    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();

      String userId = claims.get("userId").toString();
      String role = claims.get("role", String.class);

      ServerHttpRequest mutatedRequest = exchange.getRequest()
          .mutate()
          .header("X-USER-ID", userId)
          .header("X-USER-ROLE", "ROLE_" + role)
          .build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      log.error("JWT validation failed for request {}: {}", path, e.getMessage());
      return exchange.getResponse().setComplete();
    }
  }

  @Override
  public int getOrder() {
    return -1;
  }
}