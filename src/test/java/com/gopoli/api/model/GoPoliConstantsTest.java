package com.gopoli.api.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GoPoliConstantsTest {

    @Test
    void isDriver_trueOnlyForDriverUserType() {
        assertTrue(GoPoliConstants.isDriver(GoPoliConstants.USER_TYPE_DRIVER));
        assertFalse(GoPoliConstants.isDriver(GoPoliConstants.USER_TYPE_PASSENGER));
        assertFalse(GoPoliConstants.isDriver(null));
        assertFalse(GoPoliConstants.isDriver(99));
    }

    @Test
    void isDriverTrip_trueOnlyForDriverGroupTripType() {
        assertTrue(GoPoliConstants.isDriverTrip(GoPoliConstants.TRIP_TYPE_DRIVER_GROUP));
        assertFalse(GoPoliConstants.isDriverTrip(GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP));
        assertFalse(GoPoliConstants.isDriverTrip(null));
        assertFalse(GoPoliConstants.isDriverTrip(2));
    }
}
