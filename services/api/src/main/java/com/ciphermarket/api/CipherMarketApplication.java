package com.ciphermarket.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CipherMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CipherMarketApplication.class, args);
    }
}
