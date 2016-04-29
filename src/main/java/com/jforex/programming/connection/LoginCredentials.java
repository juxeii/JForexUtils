package com.jforex.programming.connection;

public final class LoginCredentials {

    private final String jnlpAddress;
    private final String username;
    private final String password;
    private final String pin;

    public LoginCredentials(final String jnlpAddress,
                            final String username,
                            final String password,
                            final String pin) {
        this.jnlpAddress = jnlpAddress;
        this.username = username;
        this.password = password;
        this.pin = pin;
    }

    public LoginCredentials(final String jnlpAddress,
                            final String username,
                            final String password) {
        this(jnlpAddress, username, password, "");
    }

    public final String jnlpAddress() {
        return jnlpAddress;
    }

    public final String username() {
        return username;
    }

    public final String password() {
        return password;
    }

    public final String pin() {
        return pin;
    }
}
