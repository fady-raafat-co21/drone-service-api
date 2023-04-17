package com.example.droneservice;

import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.controller.payload.MedicationDTO;
import com.example.droneservice.exception.NotAcceptException;
import com.example.droneservice.model.Drone;
import com.example.droneservice.model.Medication;
import com.example.droneservice.model.State;
import com.example.droneservice.repo.DroneRepo;
import com.example.droneservice.repo.MedicationRepo;
import com.example.droneservice.service.DroneServiceImpl;
import jakarta.persistence.EntityExistsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.webjars.NotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DroneServiceImplTest {

    @InjectMocks
    private DroneServiceImpl droneService;
    @Mock
    private MedicationRepo medicationRepository;

    @Mock
    private DroneRepo droneRepository;

    @Test
    public void testRegisterDrone_Success() {
        // Create a sample DroneDTO object
        DroneDTO droneDTO = new DroneDTO();
        droneDTO.setSerialNumber("1234567890");
        droneDTO.setBattery(80);
        droneDTO.setWeight(200L);
        // Set up repository mock to return false when checking for existing serial number
        when(droneRepository.existsById(droneDTO.getSerialNumber())).thenReturn(false);
        // Set up repository mock to return a Drone entity when saving
        when(droneRepository.save(any(Drone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the registerDrone method
        DroneDTO result = droneService.registerDrone(droneDTO);

        // Verify that the repository methods were called correctly
        verify(droneRepository).existsById(droneDTO.getSerialNumber());
        verify(droneRepository).save(any(Drone.class));

        // Assert that the returned DroneDTO object is not null
        assertNotNull(result);
        // Assert that the returned DroneDTO object has the same serial number as the input DTO
        assertEquals(droneDTO.getSerialNumber(), result.getSerialNumber());
    }

    @Test
    public void testRegisterDrone_Failure_SerialNumberExists() {
        // Create a sample DroneDTO object
        DroneDTO droneDTO = new DroneDTO();
        droneDTO.setSerialNumber("1234567890");
        droneDTO.setBattery(80);
        droneDTO.setWeight(200L);

        // Set up repository mock to return true when checking for existing serial number
        when(droneRepository.existsById(droneDTO.getSerialNumber())).thenReturn(true);

        // Call the registerDrone method and expect EntityExistsException to be thrown
        assertThrows(EntityExistsException.class, () -> droneService.registerDrone(droneDTO));

        // Verify that the repository methods were called correctly
        verify(droneRepository).existsById(droneDTO.getSerialNumber());
        verify(droneRepository, never()).save(any(Drone.class));
    }

    @Test
    public void testLoadDrone() throws NotAcceptException {
        // Arrange
        String serialNumber = "1234567890";
        MedicationDTO medicationDTO = new MedicationDTO();
        medicationDTO.setWeight(1L);
        medicationDTO.setName("name");
        medicationDTO.setCode("DSDSD");

        Drone drone = new Drone();
        drone.setState(State.IDLE);
        drone.setBattery(50);
        when(droneRepository.findById(serialNumber)).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Act
        MedicationDTO result = droneService.loadDrone(serialNumber, medicationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(medicationDTO, result);
        assertEquals(State.LOADING, drone.getState());
        verify(medicationRepository, times(1)).save(any(Medication.class));
        verify(droneRepository, times(1)).save(any(Drone.class));
    }
    @Test
    public void testLoadDrone_DroneNotFound() throws NotAcceptException {
        String serialNumber = "123";
        MedicationDTO medicationDTO = new MedicationDTO();
        when(droneRepository.findById(serialNumber)).thenReturn(Optional.empty());

        // Act
        assertThrows(NotFoundException.class, () -> droneService.loadDrone(serialNumber, medicationDTO));

    }
    @Test
    public void testLoadDrone_DroneStateNotIdleOrLoading() throws NotAcceptException {
        // Arrange
        String serialNumber = "1234567890";
        MedicationDTO medicationDTO = new MedicationDTO();
        Drone drone = new Drone();
        drone.setWeightLimit(200L);
        drone.setBattery(50);
        drone.setState(State.LOADED);
        when(droneRepository.findById(serialNumber)).thenReturn(Optional.of(drone));

        assertThrows(NotAcceptException.class, () -> droneService.loadDrone(serialNumber, medicationDTO));

    }
    @Test
    public void testLoadDrone_BatteryLevelBelow25Percent() {
        // Arrange
        String serialNumber = "123";
        MedicationDTO medicationDTO = new MedicationDTO();
        Drone drone = new Drone();
        drone.setState(State.IDLE);
        drone.setBattery(22);
        when(droneRepository.findById(serialNumber)).thenReturn(Optional.of(drone));

        // Act & Assert
        assertThrows(NotAcceptException.class, () -> droneService.loadDrone(serialNumber, medicationDTO));

        // Verify that the droneRepository.save() was not called
        verify(droneRepository, times(0)).save(any(Drone.class));
    }

    // Test case for changing state to LOADED with invalid current state
    @Test
    public void testChangeStateToLoadedWithInvalidCurrentState() {
        // Mocking the Drone object
        Drone drone = new Drone();
        drone.setState(State.IDLE);
        when(droneRepository.findById(anyString())).thenReturn(Optional.of(drone));

        assertThrows(NotAcceptException.class, () -> droneService.changeStateOfDrone("serialNo", State.IDLE));

    }

    // Test case for successful change of state to DELIVERING
    @Test
    public void testChangeStateToDeliveringSuccess() throws NotAcceptException {
        // Mocking the Drone object
        Drone drone = new Drone();
        drone.setState(State.LOADED);
        when(droneRepository.findById(anyString())).thenReturn(Optional.of(drone));

        droneService.changeStateOfDrone("serialNo", State.DELIVERING);

        // Assert the expected results
        assertEquals(State.DELIVERING, drone.getState());
    }


}
