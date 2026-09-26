package com.gopoli.api.dto;

public final class UserDtos {

    private UserDtos() {
    }

    public record UpdateProfile(String name, String phone, String email, Integer programId) {
    }

    public record UpdatePhoto(String photoBase64) {
    }

    public record RegisterDriver(String brand, String model, String color, String plate) {
    }
}
