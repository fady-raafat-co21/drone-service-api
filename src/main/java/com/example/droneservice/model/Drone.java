package com.example.droneservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "drone")
public class Drone {
    @Id
    private String serialNumber;
    @Enumerated(EnumType.STRING)
    private Model model;
    private Long weightLimit;
    private Integer battery;
    @Enumerated(EnumType.STRING)
    private State state;
    @OneToMany()
    @JoinColumn(name = "drone_id")

    private List<Medication> medicationList =new ArrayList<>();

}
