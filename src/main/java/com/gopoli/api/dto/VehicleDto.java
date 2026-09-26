package com.gopoli.api.dto;

import com.gopoli.api.model.Vehicle;

public record VehicleDto(Integer id, String brand, String model, String color, String plate) {

    public static VehicleDto from(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleDto(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getPlate());
    }
}
