package com.innowise.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

  private final ObjectMapper objectMapper;

  public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  @NonNull
  public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
    ServerHttpResponse response = exchange.getResponse();
    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    HttpStatus status = resolveStatus(ex);
    return buildResponse(exchange, response, status, ex.getMessage());
  }

  private HttpStatus resolveStatus(Throwable ex) {
    if (ex instanceof JwtException) {
      return HttpStatus.UNAUTHORIZED;
    }
    if (ex instanceof ResponseStatusException responseStatusException) {
      return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  private Mono<Void> buildResponse(ServerWebExchange exchange, ServerHttpResponse response,
      HttpStatus status, String message) {
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    response.setStatusCode(status);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .timestamp(LocalDateTime.now().toString())
        .build();

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
      DataBuffer buffer = response.bufferFactory().wrap(bytes);
      return response.writeWith(Mono.just(buffer));
    } catch (JsonProcessingException e) {
      String rawError = String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
          status.getReasonPhrase(), message);
      DataBuffer buffer = response.bufferFactory()
          .wrap(rawError.getBytes(StandardCharsets.UTF_8));
      return response.writeWith(Mono.just(buffer));
    }
  }
}