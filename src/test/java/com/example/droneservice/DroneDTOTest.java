package com.example.droneservice;


import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.model.Model;
import com.example.droneservice.model.State;
import com.example.droneservice.util.AppConstantsUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DroneDTOTest {



    private static final int DRONE_SERIAL_NUMBER_LIMIT = 100;
    private static final int WEIGHT_MAX = AppConstantsUtil.WEIGHT_MAX;
    private static final int BATTERY_MAX = AppConstantsUtil.BATTERY_MAX;

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testValidDroneDTO() {
        // Create a valid DroneDTO object
        DroneDTO drone = new DroneDTO();
        drone.setSerialNumber("SN12345");
        drone.setModel(Model.CRUISERWEIGHT);
        drone.setWeight(250L);
        drone.setBattery(80);
        drone.setState(State.IDLE);

        // Validate the DroneDTO object
        Set<ConstraintViolation<DroneDTO>> violations = validator.validate(drone);
        assertTrue(violations.isEmpty(), "Expected no violations for a valid DroneDTO");
    }

    @Test
    public void testInvalidDroneDTO() {
        // Create an invalid DroneDTO object
        DroneDTO drone = new DroneDTO();
        drone.setSerialNumber(""); // blank
        drone.setModel(null); // null
        drone.setWeight(540L); // weight exceeding limit
        drone.setBattery(120); // battery exceeding limit
        drone.setState(null); // null

        // Validate the DroneDTO object
        Set<ConstraintViolation<DroneDTO>> violations = validator.validate(drone);
        assertFalse(violations.isEmpty(), "Expected violations for an invalid DroneDTO");

        // Assert specific violation messages
        assertEquals("Expected 5 violations for an invalid DroneDTO", violations.size(), 5);
        for (ConstraintViolation<DroneDTO> violation : violations) {
            String propertyName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            switch (propertyName) {
                case "serialNumber":
                    assertTrue(message.contains("must not be blank"), "Expected 'serialNumber' violation message");
                    break;
                case "model":
                    assertTrue(message.contains("must not be null"), "Expected 'model' violation message");
                    break;
                case "weight":
                    assertTrue(message.contains("must be less than or equal to " + WEIGHT_MAX), "Expected 'weight' violation message");
                    break;
                case "battery":
                    assertTrue(message.contains("must be less than or equal to " + BATTERY_MAX), "Expected 'battery' violation message");
                    break;
                case "state":
                    assertTrue(message.contains("must not be null"), "Expected 'state' violation message");
                    break;
                default:
                    fail("Unexpected violation for property: " + propertyName);
                    break;
            }
        }
    }

}


