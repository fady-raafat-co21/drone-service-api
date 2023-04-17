package com.example.droneservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DroneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneServiceApplication.class, args);
    }

}
