package com.innowise.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

  private String message;
  private int status;
  private String error;
  private String timestamp;
}