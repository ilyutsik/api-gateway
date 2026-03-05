package com.innowise.apigateway.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ErrorResponse> handleWebClientResponseException(WebClientResponseException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(ex.getResponseBodyAsString());
      String message = node.get("message").asText();

      return buildResponse(message, status);
    } catch (Exception e) {
      return buildResponse(e.getMessage(), status);
    }
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
    ErrorResponse response = new ErrorResponse(message, status.value(), status.getReasonPhrase(),
        LocalDateTime.now().toString());
    return new ResponseEntity<>(response, status);
  }
}