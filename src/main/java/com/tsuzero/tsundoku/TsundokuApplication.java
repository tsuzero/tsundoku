package com.tsuzero.tsundoku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TsundokuApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsundokuApplication.class, args);
    }
}
