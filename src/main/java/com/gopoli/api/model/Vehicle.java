package com.gopoli.api.model;

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
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "brand", length = 60)
    private String brand;

    @Column(name = "model", length = 60)
    private String model;

    @Column(name = "plate", length = 20)
    private String plate;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;
}
