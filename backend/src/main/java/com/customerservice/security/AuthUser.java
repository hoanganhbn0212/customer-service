package com.customerservice.security;

public record AuthUser(String id, String username, String role) {

    public boolean isAdmin() {
        return AppRoles.ADMIN.equals(role);
    }

    public boolean isDevelop() {
        return AppRoles.DEVELOP.equals(role);
    }

    /** Admin or Develop — may edit login theme / page assets */
    public boolean canEditPages() {
        return isAdmin() || isDevelop();
    }
}
