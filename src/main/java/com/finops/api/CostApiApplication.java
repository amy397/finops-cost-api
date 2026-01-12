package com.finops.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CostApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CostApiApplication.class, args);
    }
}
