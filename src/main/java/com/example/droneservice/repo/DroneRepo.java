package com.example.droneservice.repo;

import com.example.droneservice.model.Drone;
import com.example.droneservice.model.State;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DroneRepo extends JpaRepository<Drone,String> {


    List<Drone> findByStateAndBatteryGreaterThan(State idle,Long battery);

}
