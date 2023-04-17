package com.example.droneservice;

import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.controller.payload.MedicationDTO;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MedicationDTOTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidMedicationDTO() {
        MedicationDTO medicationDTO = new MedicationDTO();
        medicationDTO.setName("medication");
        medicationDTO.setWeight(100L);
        medicationDTO.setCode("CODE123");

        Set<ConstraintViolation<MedicationDTO>> violations = validator.validate(medicationDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidMedicationDTO() {
        MedicationDTO medicationDTO = new MedicationDTO();
        // Invalid name (contains invalid characters)
        medicationDTO.setName("medication@123");
        // Invalid weight (null)
        medicationDTO.setWeight(null);
        // Invalid code (contains invalid characters)
        medicationDTO.setCode("Code@123");

        Set<ConstraintViolation<MedicationDTO>> violations = validator.validate(medicationDTO);
        assertEquals(3, violations.size());

        // Assert violations for each invalid field
        ConstraintViolation<MedicationDTO> nameViolation = getViolationByPropertyPath(violations, "name");
        assertNotNull(nameViolation);
        assertEquals("must match \"^[a-zA-Z0-9_\\-]*\"", nameViolation.getMessage());

        ConstraintViolation<MedicationDTO> weightViolation = getViolationByPropertyPath(violations, "weight");
        assertNotNull(weightViolation);
        assertEquals("Weight must not be null", weightViolation.getMessage());

        ConstraintViolation<MedicationDTO> codeViolation = getViolationByPropertyPath(violations, "code");
        assertNotNull(codeViolation);
        assertEquals("must match \"^[A-Z0-9_]*\"", codeViolation.getMessage());
    }

    private ConstraintViolation<MedicationDTO> getViolationByPropertyPath(Set<ConstraintViolation<MedicationDTO>> violations, String propertyPath) {
        return violations.stream()
                .filter(violation -> propertyPath.equals(violation.getPropertyPath().toString()))
                .findFirst()
                .orElse(null);
    }
}

