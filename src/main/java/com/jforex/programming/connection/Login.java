package com.jforex.programming.connection;

import java.util.Optional;

import com.dukascopy.api.system.IClient;

public final class Login {

    private final IClient client;

    public Login(final IClient client) {
        this.client = client;
    }

    public final Optional<Exception> withoutPin(final String jnlpAddress,
                                                final String username,
                                                final String password) {
        try {
            client.connect(jnlpAddress, username, password);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final Optional<Exception> withPin(final String jnlpAddress,
                                             final String username,
                                             final String password,
                                             final String pin) {
        try {
            client.connect(jnlpAddress, username, password, pin);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final void logout() {
        client.disconnect();
    }
}
