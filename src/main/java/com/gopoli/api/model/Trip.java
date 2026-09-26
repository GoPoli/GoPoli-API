package com.gopoli.api.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "description")
    private String description;

    @Column(name = "departure_location_id")
    private Integer departureLocationId;

    @Column(name = "arrival_location_id")
    private Integer arrivalLocationId;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "trip_type_id")
    private Integer tripTypeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "capacity")
    private Integer capacity;
}
