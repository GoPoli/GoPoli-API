package com.gopoli.api.policy;

import java.util.Locale;

public final class ProfilePolicy {

    public static final String INSTITUTIONAL_DOMAIN = "@elpoli.edu.co";

    private ProfilePolicy() {
    }

    public static String emailError(String email) {
        String normalized = normalizeEmail(email);
        if (normalized == null || !normalized.endsWith(INSTITUTIONAL_DOMAIN)) {
            return "Usa tu correo " + INSTITUTIONAL_DOMAIN;
        }
        return null;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static String nameError(String name) {
        int length = name == null ? 0 : name.trim().length();
        if (length < 3 || length > 120) {
            return "El nombre debe tener entre 3 y 120 caracteres";
        }
        return null;
    }

    public static String phoneError(String phone) {
        if (phone == null || !phone.matches("\\d{8,14}")) {
            return "El teléfono debe contener entre 8 y 14 dígitos";
        }
        return null;
    }
}
