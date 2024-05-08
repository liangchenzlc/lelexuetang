package com.lelexuetang.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.ControllerAdvice;

@SpringBootApplication
@ControllerAdvice("com.lelexuetang.media.config")
public class MediaTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaTestApplication.class, args);
    }
}
