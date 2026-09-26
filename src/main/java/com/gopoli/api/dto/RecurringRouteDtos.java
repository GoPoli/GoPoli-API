package com.gopoli.api.dto;

import java.time.LocalTime;

public final class RecurringRouteDtos {

    private RecurringRouteDtos() {
    }

    public record RouteRequest(
            Integer departureLocationId,
            Integer arrivalLocationId,
            String weekdays,
            LocalTime departureTime,
            Integer capacity,
            Integer tripTypeId,
            String description) {
    }

    public record RouteResponse(
            Integer id,
            Integer userId,
            Integer departureLocationId,
            Integer arrivalLocationId,
            String weekdays,
            LocalTime departureTime,
            Integer capacity,
            Integer tripTypeId,
            String description,
            String departureLocationName,
            String arrivalLocationName) {
    }
}
