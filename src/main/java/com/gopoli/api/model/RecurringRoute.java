package com.gopoli.api.model;

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
@Table(name = "recurring_routes")
@Getter
@Setter
@NoArgsConstructor
public class RecurringRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "departure_location_id")
    private Integer departureLocationId;

    @Column(name = "arrival_location_id")
    private Integer arrivalLocationId;

    // Días ISO (1 = lunes … 7 = domingo) separados por coma, p. ej. "1,2,3,4,5".
    @Column(name = "weekdays")
    private String weekdays;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "trip_type_id")
    private Integer tripTypeId;

    @Column(name = "description")
    private String description;
}
