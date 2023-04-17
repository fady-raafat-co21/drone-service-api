package com.example.droneservice.controller.payload;

import com.example.droneservice.model.Model;
import com.example.droneservice.model.State;
import jakarta.validation.constraints.*;
import lombok.*;

import static com.example.droneservice.util.AppConstantsUtil.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DroneDTO {
    @NotBlank
    @NotNull
    @Size(max = DRONE_SERIAL_NUMBER_LIMIT)
    private String serialNumber;
    @NotNull
    private Model model;
    @NotNull
    @Max(value = WEIGHT_MAX)
    @Positive()
    private Long weight;
    @NotNull
    @Max(value = BATTERY_MAX)
    @Positive
    private Integer battery;
    @NotNull
    private State state;
}
