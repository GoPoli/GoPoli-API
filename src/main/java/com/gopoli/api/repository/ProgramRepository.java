package com.gopoli.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.Program;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
}
