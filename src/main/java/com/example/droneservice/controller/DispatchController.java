package com.example.droneservice.controller;

import com.example.droneservice.controller.payload.DroneDTO;
import com.example.droneservice.controller.payload.MedicationDTO;
import com.example.droneservice.exception.ExceptionResponse;
import com.example.droneservice.exception.NotAcceptException;
import com.example.droneservice.model.State;
import com.example.droneservice.service.DroneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLDataException;
import java.util.List;

import static com.example.droneservice.util.AppConstantsUtil.*;
import static org.springframework.http.MediaType.*;

@RestController
@Tag(name = "Dispatch Controller", description = "Drones Service API")
@RequestMapping("/api/v1/drones")
public class DispatchController {
    private static final Logger logger =
            LoggerFactory.getLogger(DispatchController.class);
    private static final String NEW_DRONE_LOG = "New drone was created :{}";
    private static final String NEW_MEDICATION_LOG = "New medication was created :{}";

    private final DroneService droneService;

    @Autowired
    public DispatchController(DroneService droneService) {
        this.droneService = droneService;
    }

    @Operation(summary = "Register a new drone by DroneDTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Register a new drone", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DroneDTO.class))}),
            @ApiResponse(responseCode = "409", description = "if you try to save same drone again", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping(value = "/register", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DroneDTO> registerDrone(@RequestBody @Valid @NotNull DroneDTO droneDTO) throws SQLDataException {
        final DroneDTO createdDrone = droneService.registerDrone(droneDTO);
        logger.info(NEW_DRONE_LOG, createdDrone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(droneDTO);
    }

    @Operation(summary = "loading a drone with medication by serialNo and MedicationDTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "loading a drone with medication", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = MedicationDTO.class))}),
            @ApiResponse(responseCode = "404", description = "if drone not found", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "406", description = "if battery less than :" + BATTERY_LIMIT + " , or state not IDLE OR LOADING ", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/load", consumes = "multipart/form-data", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MedicationDTO> loadDrone(@RequestParam @NotNull @Size(max = DRONE_SERIAL_NUMBER_LIMIT) String serialNo,
                                                   @RequestParam MultipartFile image,
                                                   @RequestParam @Size(max = MEDICATION_NAME_MAX) @Pattern(regexp = MEDICATION_NAME_PATTERN) String name,
                                                   @RequestParam Long weight,
                                                   @RequestParam @Size(max = MEDICATION_CODE_MAX) @Pattern(regexp = MEDICATION_CODE_PATTERN) String code) throws IOException, NotAcceptException {
        final MedicationDTO newMedication = droneService.loadDrone(serialNo, new MedicationDTO(name, weight, code, image.getBytes()));
        logger.info(NEW_MEDICATION_LOG, newMedication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newMedication);
    }

    @Operation(summary = "checking loaded medication items for a given drone by serialNo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "return loaded medication items for a given drone", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = MedicationDTO.class))}),
            @ApiResponse(responseCode = "404", description = "if drone not found", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @GetMapping(value = "/{serialNo}/medications", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedicationDTO>> checkMedicationsForGivenDrone(@PathVariable @NotNull @NotBlank @Size(max = DRONE_SERIAL_NUMBER_LIMIT) String serialNo) {
        return ResponseEntity.status(HttpStatus.OK).body(droneService.retrieveMedications(serialNo));
    }

    @Operation(summary = "checking available drones for loading")
    @ApiResponse(responseCode = "200", description = "return available drones for loading;", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DroneDTO.class))})
    @GetMapping(value = "/availabilities", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DroneDTO>> availableDronesForLoading() {
        return ResponseEntity.status(HttpStatus.OK).body(droneService.getAvailableDrones());
    }

    @Operation(summary = "check drone battery level for a given drone by serialNo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "return drone battery level for a given drone", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class))}),
            @ApiResponse(responseCode = "404", description = "if drone not found", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @GetMapping(value = "/batteries/{serialNo}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> batteryLevelOfDrone(@PathVariable @NotNull @NotBlank @Size(max = DRONE_SERIAL_NUMBER_LIMIT) String serialNo) {
        return ResponseEntity.status(HttpStatus.OK).body(droneService.getBatteryLevel(serialNo));
    }

    @Operation(summary = "change state of drone by serialNO ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "change state of drone by serialNO", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "if drone not found", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "406", description = "if state not right you can check error message for track this", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PatchMapping(value = "/{serialNo}/{state}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changeStateOfDone(@PathVariable @NotNull @NotBlank State state,
                                                    @PathVariable @NotNull @NotBlank @Size(max = DRONE_SERIAL_NUMBER_LIMIT) String serialNo) throws NotAcceptException {
        droneService.changeStateOfDrone(serialNo, state);
        return ResponseEntity.status(HttpStatus.OK).body("State of Drone is changed");
    }

}
