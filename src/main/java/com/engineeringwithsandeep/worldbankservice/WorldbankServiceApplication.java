package com.engineeringwithsandeep.worldbankservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WorldbankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorldbankServiceApplication.class, args);
    }

}
