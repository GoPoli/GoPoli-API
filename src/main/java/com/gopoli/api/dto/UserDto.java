package com.gopoli.api.dto;

import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.User;
import com.gopoli.api.model.Vehicle;

public record UserDto(
        Integer id,
        String email,
        String name,
        String phone,
        Integer programId,
        Integer statusId,
        Integer userTypeId,
        Double rating,
        String profilePhoto,
        boolean driver,
        VehicleDto vehicle) {

    public static UserDto from(User user) {
        return from(user, null);
    }

    public static UserDto from(User user, Vehicle vehicle) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProgramId(),
                user.getStatusId(),
                user.getUserTypeId(),
                user.getRating(),
                user.getProfilePhoto(),
                GoPoliConstants.isDriver(user.getUserTypeId()),
                VehicleDto.from(vehicle));
    }
}
