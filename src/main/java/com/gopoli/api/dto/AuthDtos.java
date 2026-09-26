package com.gopoli.api.dto;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record Login(String email, String password) {
    }

    public record Register(String email, String password, String name, String phone, Integer programId) {
    }

    public record LoginResponse(String token, UserDto user) {
    }
}
