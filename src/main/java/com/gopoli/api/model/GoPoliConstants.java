package com.gopoli.api.model;

public final class GoPoliConstants {

    private GoPoliConstants() {
    }

    public static final int USER_TYPE_PASSENGER = 1;
    public static final int USER_TYPE_DRIVER = 2;

    public static final int TRIP_TYPE_PASSENGER_GROUP = 1;
    public static final int TRIP_TYPE_DRIVER_GROUP = 3;

    public static final int TRIP_STATUS_ACTIVE = 1;
    public static final int TRIP_STATUS_CANCELLED = 2;
    public static final int TRIP_STATUS_FINISHED = 3;
    public static final int TRIP_STATUS_IN_PROGRESS = 4;

    public static final String GROUP_ROLE_CREATOR = "creator";
    public static final String GROUP_ROLE_MEMBER = "member";

    public static final String PARTICIPATION_PASSENGER = "passenger";
    public static final String PARTICIPATION_DRIVER = "driver";

    public static boolean isDriver(Integer userTypeId) {
        return userTypeId != null && userTypeId == USER_TYPE_DRIVER;
    }

    public static boolean isDriverTrip(Integer tripTypeId) {
        return tripTypeId != null && tripTypeId == TRIP_TYPE_DRIVER_GROUP;
    }
}
