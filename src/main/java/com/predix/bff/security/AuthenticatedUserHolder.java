package com.predix.bff.security;

public final class AuthenticatedUserHolder {

    private static final ThreadLocal<SessionUser> HOLDER = new ThreadLocal<>();

    private AuthenticatedUserHolder() {}

    public static void set(SessionUser user) {
        HOLDER.set(user);
    }

    public static SessionUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
