package com.innowise.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Gatewayservice {

  public static void main(String[] args) {
    SpringApplication.run(Gatewayservice.class, args);
  }
}