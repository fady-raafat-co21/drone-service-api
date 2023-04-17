package com.example.droneservice.repo;

import com.example.droneservice.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepo extends JpaRepository<Medication, Long> {
}
