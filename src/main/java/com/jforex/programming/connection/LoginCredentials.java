package com.jforex.programming.connection;

import java.util.Optional;

public final class LoginCredentials {

    private final String jnlpAddress;
    private final String username;
    private final String password;
    private final Optional<String> pinOpt;

    public LoginCredentials(final String jnlpAddress,
                            final String username,
                            final String password,
                            final String pin) {
        this.jnlpAddress = jnlpAddress;
        this.username = username;
        this.password = password;
        pinOpt = pin.isEmpty()
                ? Optional.empty()
                : Optional.of(pin);
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

    public final Optional<String> pinOpt() {
        return pinOpt;
    }
}
