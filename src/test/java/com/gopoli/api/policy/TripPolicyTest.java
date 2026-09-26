package com.gopoli.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.gopoli.api.model.GoPoliConstants;

class TripPolicyTest {

    @Test
    void allowsStartingOnlyFromPlanning() {
        assertNull(TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_ACTIVE, GoPoliConstants.TRIP_STATUS_IN_PROGRESS));
        assertEquals("El viaje solo puede iniciarse desde planificación", TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_IN_PROGRESS, GoPoliConstants.TRIP_STATUS_IN_PROGRESS));
    }

    @Test
    void allowsFinishingOnlyTripsInProgress() {
        assertNull(TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_IN_PROGRESS, GoPoliConstants.TRIP_STATUS_FINISHED));
        assertEquals("Solo un viaje en curso puede finalizarse", TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_ACTIVE, GoPoliConstants.TRIP_STATUS_FINISHED));
    }

    @Test
    void allowsCancellingOnlyWhilePlanning() {
        assertNull(TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_ACTIVE, GoPoliConstants.TRIP_STATUS_CANCELLED));
        assertEquals("Solo un viaje en planificación puede cancelarse", TripPolicy.transitionError(
                GoPoliConstants.TRIP_STATUS_IN_PROGRESS, GoPoliConstants.TRIP_STATUS_CANCELLED));
    }

    @Test
    void validatesLocationsAndCapacity() {
        assertEquals("Salida y llegada deben ser distintas", TripPolicy.creationError(4, 4, 3));
        assertEquals("La capacidad debe ser entre 2 y 4 personas", TripPolicy.creationError(4, 5, 1));
        assertNull(TripPolicy.creationError(4, 5, 4));
    }

    @Test
    void acceptsOnlyKnownTripTypes() {
        assertTrue(TripPolicy.isValidTripType(GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP));
        assertTrue(TripPolicy.isValidTripType(GoPoliConstants.TRIP_TYPE_DRIVER_GROUP));
        assertFalse(TripPolicy.isValidTripType(2));
        assertFalse(TripPolicy.isValidTripType(null));
    }

    @Test
    void limitsDescriptionLength() {
        assertNull(TripPolicy.descriptionError(null));
        assertNull(TripPolicy.descriptionError("a".repeat(500)));
        assertEquals("La descripción no puede superar los 500 caracteres",
                TripPolicy.descriptionError("a".repeat(501)));
    }

    @Test
    void normalizesBlankDescriptionToNull() {
        assertNull(TripPolicy.normalizeDescription("   "));
        assertEquals("Salgo puntual", TripPolicy.normalizeDescription("  Salgo puntual "));
    }
}
