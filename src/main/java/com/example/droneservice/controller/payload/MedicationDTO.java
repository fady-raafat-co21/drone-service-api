package com.example.droneservice.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import static com.example.droneservice.util.AppConstantsUtil.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationDTO {

    @NotBlank @NotNull @Size(max = MEDICATION_NAME_MAX)@Pattern(regexp = MEDICATION_NAME_PATTERN)
    private String name;//ame (allowed only letters, numbers, ‘-‘, ‘_’);
    @NotNull(message = "Weight must not be null")
    private Long weight;
    @NotBlank @NotNull @Size(max = MEDICATION_CODE_MAX)@Pattern(regexp =MEDICATION_CODE_PATTERN )
    private String code;//code (allowed only upper case letters, underscore and numbers);
    @ToString.Exclude
    private byte[] image;


}
