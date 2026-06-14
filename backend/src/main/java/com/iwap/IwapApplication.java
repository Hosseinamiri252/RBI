package com.iwap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(IwapApplication.class, args);
    }
}
