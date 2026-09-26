package com.gopoli.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.Trip;

public final class TripDtos {

    private TripDtos() {
    }

    public record CreateTrip(
            LocalDate departureDate,
            String description,
            Integer departureLocationId,
            Integer arrivalLocationId,
            LocalTime departureTime,
            Integer tripTypeId,
            Integer capacity) {
    }

    public record TripResponse(
            Integer id,
            LocalDate departureDate,
            String description,
            Integer departureLocationId,
            Integer arrivalLocationId,
            LocalTime departureTime,
            Integer creatorId,
            Integer tripTypeId,
            Integer statusId,
            Integer capacity,
            String tripType,
            String tripTypeLabel) {

        public static TripResponse from(Trip trip) {
            return new TripResponse(
                    trip.getId(),
                    trip.getDepartureDate(),
                    trip.getDescription(),
                    trip.getDepartureLocationId(),
                    trip.getArrivalLocationId(),
                    trip.getDepartureTime(),
                    trip.getCreatorId(),
                    trip.getTripTypeId(),
                    trip.getStatusId(),
                    trip.getCapacity(),
                    TripDtos.tripTypeKey(trip.getTripTypeId()),
                    TripDtos.tripTypeLabel(trip.getTripTypeId()));
        }
    }

    public record MemberResponse(
            Integer userId,
            String groupRole,
            String participationRole,
            String participationRoleLabel,
            String userName) {
    }

    public record HistoryItem(
            Integer tripId,
            LocalDate departureDate,
            LocalTime departureTime,
            String description,
            Integer tripTypeId,
            String tripType,
            String tripTypeLabel,
            Integer departureLocationId,
            Integer arrivalLocationId,
            String departureLocationName,
            String arrivalLocationName,
            String myParticipationRole,
            String myParticipationRoleLabel,
            List<MemberResponse> participants) {
    }

    public static String tripTypeKey(Integer tripTypeId) {
        return GoPoliConstants.isDriverTrip(tripTypeId) ? "driver_group" : "passenger_group";
    }

    public static String tripTypeLabel(Integer tripTypeId) {
        return GoPoliConstants.isDriverTrip(tripTypeId) ? "Grupo conductor" : "Grupo de viaje";
    }

    public static String participationLabel(String participationRole) {
        return GoPoliConstants.PARTICIPATION_DRIVER.equals(participationRole) ? "Conductor" : "Pasajero";
    }
}
