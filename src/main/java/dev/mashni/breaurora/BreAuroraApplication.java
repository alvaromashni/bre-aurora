package dev.mashni.breaurora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BreAuroraApplication {

    public static void main(String[] args) {
        SpringApplication.run(BreAuroraApplication.class, args);
    }

}
