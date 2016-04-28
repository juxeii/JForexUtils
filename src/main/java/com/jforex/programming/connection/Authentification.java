package com.jforex.programming.connection;

import java.util.Optional;

import com.dukascopy.api.system.IClient;

public final class Authentification {

    private final IClient client;

    public Authentification(final IClient client) {
        this.client = client;
    }

    public final Optional<Exception> login(final String jnlp,
                                           final String username,
                                           final String password) {
        try {
            client.connect(jnlp, username, password);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final Optional<Exception> loginWithPin(final String jnlp,
                                                  final String username,
                                                  final String password,
                                                  final String pin) {
        try {
            client.connect(jnlp, username, password, pin);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final void logout() {
        client.disconnect();
    }
}
