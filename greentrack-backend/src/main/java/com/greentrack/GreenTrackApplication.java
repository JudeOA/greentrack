package com.greentrack;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication @EnableAsync
public class GreenTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenTrackApplication.class, args);
    }
}