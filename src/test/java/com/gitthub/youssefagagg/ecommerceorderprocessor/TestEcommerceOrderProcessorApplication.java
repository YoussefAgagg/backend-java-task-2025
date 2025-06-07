package com.gitthub.youssefagagg.ecommerceorderprocessor;

import org.springframework.boot.SpringApplication;

public class TestEcommerceOrderProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.from(EcommerceOrderProcessorApplication::main).with(
        TestcontainersConfiguration.class).run(args);
  }

}
