package com.customerservice.security;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT.set(user);
    }

    public static AuthUser get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static AuthUser requireAuthenticated() {
        AuthUser user = CURRENT.get();
        if (user == null) {
            throw new com.customerservice.exception.UnauthorizedException();
        }
        return user;
    }

    public static AuthUser requireAdmin() {
        AuthUser user = requireAuthenticated();
        if (!user.isAdmin()) {
            throw new com.customerservice.exception.ForbiddenException();
        }
        return user;
    }

    public static AuthUser requirePageEditor() {
        AuthUser user = requireAuthenticated();
        if (!user.canEditPages()) {
            throw new com.customerservice.exception.ForbiddenException();
        }
        return user;
    }
}
