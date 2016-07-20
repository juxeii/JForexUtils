package com.jforex.programming.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

public final class LoginCredentials {

    private final String jnlpAddress;
    private final String username;
    private final String password;
    private final Optional<String> maybePin;

    public LoginCredentials(final String jnlpAddress,
                            final String username,
                            final String password,
                            final String pin) {
        this.jnlpAddress = checkNotNull(jnlpAddress);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        maybePin = checkNotNull(pin).isEmpty()
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

    public final Optional<String> maybePin() {
        return maybePin;
    }
}
