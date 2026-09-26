package com.gopoli.api.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.model.Location;
import com.gopoli.api.model.Program;
import com.gopoli.api.repository.LocationRepository;
import com.gopoli.api.repository.ProgramRepository;

@RestController
public class CatalogController {

    private static final CacheControl CATALOG_CACHE = CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic();

    private final ProgramRepository programRepository;
    private final LocationRepository locationRepository;

    public CatalogController(ProgramRepository programRepository, LocationRepository locationRepository) {
        this.programRepository = programRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/programs")
    public ResponseEntity<List<Program>> programs() {
        return ResponseEntity.ok().cacheControl(CATALOG_CACHE).body(programRepository.findAll(Sort.by("id")));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> locations() {
        return ResponseEntity.ok().cacheControl(CATALOG_CACHE).body(locationRepository.findAll(Sort.by("id")));
    }
}
