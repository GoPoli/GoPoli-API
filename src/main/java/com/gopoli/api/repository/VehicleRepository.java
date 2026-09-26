package com.gopoli.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    Optional<Vehicle> findByUserId(Integer userId);

    boolean existsByPlate(String plate);
}
