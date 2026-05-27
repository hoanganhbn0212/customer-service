package com.customerservice.security;

public final class AppRoles {

    public static final String ADMIN = "ADMIN";
    public static final String DEVELOP = "DEVELOP";
    public static final String USER = "USER";

    private AppRoles() {
    }

    public static boolean isValid(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String value = role.trim().toUpperCase();
        return ADMIN.equals(value) || DEVELOP.equals(value) || USER.equals(value);
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        String value = role.trim().toUpperCase();
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        return value;
    }
}
