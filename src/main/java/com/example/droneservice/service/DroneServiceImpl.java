package com.example.droneservice.service;

import com.example.droneservice.util.AppConstantsUtil;
import com.example.droneservice.exception.NotAcceptException;
import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.controller.payload.MedicationDTO;
import com.example.droneservice.model.Drone;
import com.example.droneservice.model.Medication;
import com.example.droneservice.model.State;
import com.example.droneservice.repo.DroneRepo;
import com.example.droneservice.repo.MedicationRepo;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated

public class DroneServiceImpl implements DroneService {

    private final DroneRepo droneRepo;
    private final MedicationRepo medicationRepo;

    @Autowired
    public DroneServiceImpl(DroneRepo droneRepo, MedicationRepo medicationRepo) {
        this.droneRepo = droneRepo;
        this.medicationRepo = medicationRepo;
    }

    @Override
    public DroneDTO registerDrone(DroneDTO droneDTO) {
        String serialNumber = droneDTO.getSerialNumber();
        if (droneRepo.existsById(serialNumber)) {
            throw new EntityExistsException("Serial number already exists: " + serialNumber);
        }

        Drone drone = droneRepo.save(mapDroneToEntity(droneDTO));
        return mapDroneFromEntity(drone);
    }

    @Override
    public MedicationDTO loadDrone(String serialNumber, MedicationDTO medicationDTO) throws NotAcceptException {
        Drone drone = droneRepo.findById(serialNumber)
                .orElseThrow(() -> new NotFoundException("Drone not found")); // Throw a NotFoundException with appropriate message

        List<State> allowedStates = Arrays.asList(State.IDLE, State.LOADING); // Only IDLE and LOADING states are allowed for loading medication
        if (!allowedStates.contains(drone.getState())) {
            throw new NotAcceptException("Cannot add medication to this drone because its state is: " + drone.getState() +
                    ". It must be IDLE or LOADING.");
        }
        if (isBatteryLevelAllowLoading(serialNumber)) {
            Medication meds = Medication.builder()
                    .weight(medicationDTO.getWeight())
                    .image(medicationDTO.getImage())
                    .name(medicationDTO.getName())
                    .code(medicationDTO.getCode())
                    .build();

            List<Medication> medication = new ArrayList<>();
            medication.add(medicationRepo.save(meds));

            drone.setState(State.LOADING);
            medication.addAll(drone.getMedicationList());
            drone.setMedicationList(medication);
            droneRepo.save(drone);
            return medicationDTO;
        } else {
            throw new NotAcceptException("Battery of the drone is less than 25%. Current battery level: " + drone.getBattery() + "%");
        }
    }

    @Override
    public List<MedicationDTO> retrieveMedications(String serialNo) {
        Drone drone = droneRepo.findById(serialNo)
                .orElseThrow(() -> new NotFoundException("Drone not found"));

        return mapEntityToMedicationDTO(drone.getMedicationList());
    }

    @Override
    public List<DroneDTO> getAvailableDrones() {
        List<Drone> idleDrones = droneRepo.findByStateAndBatteryGreaterThan(State.IDLE, AppConstantsUtil.BATTERY_LIMIT);

        return idleDrones.stream()
                .map(this::mapDroneFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getBatteryLevel(String serialNo) {
        return droneRepo.findById(serialNo).orElseThrow(() -> new NotFoundException("Drone not found")).getBattery();
    }

    @Override
    public void changeStateOfDrone(String serialNo, State state) throws NotAcceptException {
        Drone drone = droneRepo.findById(serialNo)
                .orElseThrow(() -> new NotFoundException("Drone not found"));

        State currentState = drone.getState();

        switch (state) {
            case LOADED:
                if (currentState.equals(State.LOADING)) {
                    if (isBelowOrEqualDroneWeightLimit(serialNo)) {
                        drone.setState(State.LOADED);
                        droneRepo.save(drone);
                    } else
                        throw new NotAcceptException("Drone must be in LOADING state before changing to LOADED state");
                } else
                    throw new NotAcceptException("Cannot load medication items as total weight exceeds drone's weight limit");
                break;

            case DELIVERING:
                if (currentState.equals(State.LOADED)) {
                    drone.setState(State.DELIVERING);
                    droneRepo.save(drone);
                } else
                    throw new NotAcceptException("Drone must be in LOADED state before changing to DELIVERING state");
                break;

            case DELIVERED:
                if (currentState.equals(State.DELIVERING)) {
                    drone.setState(State.DELIVERED);
                    droneRepo.save(drone);
                } else
                    throw new NotAcceptException("Drone must be in DELIVERING state before changing to DELIVERED state");
                break;

            case RETURNING:
                if (currentState.equals(State.DELIVERED)) {
                    drone.setState(State.RETURNING);
                    droneRepo.save(drone);
                } else
                    throw new NotAcceptException("Drone must be in DELIVERED state before changing to RETURNING state");
                break;

            case IDLE:
                if (currentState.equals(State.RETURNING)) {
                    drone.setState(State.IDLE);
                    drone.setMedicationList(new ArrayList<>());
                    droneRepo.save(drone);
                } else
                    throw new NotAcceptException("Drone must be in RETURNING state before changing to IDLE state");
                break;

            default:
                throw new IllegalArgumentException("Invalid state: " + state);
        }
    }


    private Drone mapDroneToEntity(DroneDTO droneDTO) {
        return Drone.builder()
                .serialNumber(droneDTO.getSerialNumber())
                .model(droneDTO.getModel())
                .battery(droneDTO.getBattery())
                .state(droneDTO.getState())
                .weightLimit(droneDTO.getWeight())
                .build();
    }

    private DroneDTO mapDroneFromEntity(Drone drone) {
        return DroneDTO.builder()
                .serialNumber(drone.getSerialNumber())
                .weight(drone.getWeightLimit())
                .state(drone.getState())
                .battery(drone.getBattery())
                .model(drone.getModel())
                .build();
    }

    private List<MedicationDTO> mapEntityToMedicationDTO(List<Medication> medications) {
        return medications.stream()
                .map(m -> MedicationDTO.builder()
                        .weight(m.getWeight())
                        .name(m.getName())
                        .image(m.getImage())
                        .code(m.getCode())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isBatteryLevelAllowLoading(String serialNumber) {
        return droneRepo.findById(serialNumber).get().getBattery() >= 25;
    }

    public boolean isBelowOrEqualDroneWeightLimit(String serialNumber) {
        Drone drone = droneRepo.findById(serialNumber).orElseThrow(() -> new IllegalArgumentException("Drone not found with serial number: " + serialNumber));
        long totalMedicationWeight = drone.getMedicationList().stream().mapToLong(Medication::getWeight).sum();
        return drone.getWeightLimit() >= totalMedicationWeight;
    }

}
