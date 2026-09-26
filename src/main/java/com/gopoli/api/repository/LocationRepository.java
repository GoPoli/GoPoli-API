package com.gopoli.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.Location;

public interface LocationRepository extends JpaRepository<Location, Integer> {
}
