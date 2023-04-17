package com.example.droneservice.service;

import com.example.droneservice.exception.NotAcceptException;
import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.controller.payload.MedicationDTO;
import com.example.droneservice.model.State;

import java.util.List;
public interface DroneService {

    DroneDTO registerDrone(DroneDTO drone);

    MedicationDTO loadDrone(String serialNumber, MedicationDTO m) throws NotAcceptException;

    List<MedicationDTO> retrieveMedications(String serialNo);

    List<DroneDTO> getAvailableDrones();

    Integer getBatteryLevel(String serialNo);

    void changeStateOfDrone(String serialNo, State state) throws NotAcceptException;
}
