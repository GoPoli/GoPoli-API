package com.gopoli.api.policy;

import com.gopoli.api.model.GoPoliConstants;

public final class TripPolicy {

    public static final int MIN_CAPACITY = 2;
    public static final int MAX_CAPACITY = 4;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private TripPolicy() {
    }

    public static String transitionError(Integer currentStatus, Integer targetStatus) {
        if (targetStatus == GoPoliConstants.TRIP_STATUS_IN_PROGRESS
                && currentStatus != GoPoliConstants.TRIP_STATUS_ACTIVE) {
            return "El viaje solo puede iniciarse desde planificación";
        }
        if (targetStatus == GoPoliConstants.TRIP_STATUS_FINISHED
                && currentStatus != GoPoliConstants.TRIP_STATUS_IN_PROGRESS) {
            return "Solo un viaje en curso puede finalizarse";
        }
        if (targetStatus == GoPoliConstants.TRIP_STATUS_CANCELLED
                && currentStatus != GoPoliConstants.TRIP_STATUS_ACTIVE) {
            return "Solo un viaje en planificación puede cancelarse";
        }
        return null;
    }

    public static String creationError(
            Integer departureLocationId,
            Integer arrivalLocationId,
            Integer capacity) {
        if (departureLocationId == null) {
            return "El lugar de salida es obligatorio";
        }
        if (arrivalLocationId == null) {
            return "El lugar de llegada es obligatorio";
        }
        if (departureLocationId.equals(arrivalLocationId)) {
            return "Salida y llegada deben ser distintas";
        }
        if (capacity == null || capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            return "La capacidad debe ser entre 2 y 4 personas";
        }
        return null;
    }

    public static String descriptionError(String description) {
        if (description != null && description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            return "La descripción no puede superar los 500 caracteres";
        }
        return null;
    }

    public static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    public static boolean isValidTripType(Integer tripTypeId) {
        return tripTypeId != null
                && (tripTypeId == GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP
                        || tripTypeId == GoPoliConstants.TRIP_TYPE_DRIVER_GROUP);
    }
}
