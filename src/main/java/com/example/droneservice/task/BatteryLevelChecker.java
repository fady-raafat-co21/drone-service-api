package com.example.droneservice.task;

import com.example.droneservice.model.Drone;
import com.example.droneservice.repo.DroneRepo;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@EnableAsync
@Component
@PropertySource("classpath:application.properties")
public class BatteryLevelChecker {

    private final DroneRepo droneRepo;

    public BatteryLevelChecker(DroneRepo droneRepo) {
        this.droneRepo = droneRepo;
    }
    private static final String LOG_FILE_PATH = "battery_log.txt"; // File path for log

    @Async
    @Scheduled(fixedRateString = "${fixed_time_for_check_batteries}")
    public void checkBatteryLevel() {
        List<Drone> dronesList = droneRepo.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {

            String log = dronesList.stream()
                    .map(drone -> String.format("Drone %s: Battery Level: %d%% at %s",
                            drone.getSerialNumber(), drone.getBattery(), Instant.now()))
                    .collect(Collectors.joining(System.lineSeparator()));

            writer.write(log);
            writer.newLine();

        } catch (IOException e) {
            Logger.getLogger(Drone.class.getName()).log(Level.SEVERE, "Failed to log battery levels", e);
        }
    }
}



