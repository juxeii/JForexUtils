package com.jforex.programming.connection;

import com.dukascopy.api.system.IClient;

import io.reactivex.Completable;

public class Reconnector {

    private final IClient client;
    private final Authentification authentification;
    private final ConnectionMonitor connectionMonitor;

    public Reconnector(final IClient client,
                       final Authentification authentification,
                       final ConnectionMonitor connectionMonitor) {
        this.client = client;
        this.authentification = authentification;
        this.connectionMonitor = connectionMonitor;
    }

    public Completable lightReconnect() {
        return Completable
            .fromAction(() -> client.reconnect())
            .andThen(connectionMonitor.observe())
            .ignoreElements();
    }

    public Completable relogin(final LoginCredentials loginCredentials) {
        return authentification
            .login(loginCredentials)
            .andThen(connectionMonitor.observe())
            .ignoreElements();
    }

    public void applyStrategy(final Completable reconnectStrategy) {
        connectionMonitor
            .observe()
            .subscribe(item -> {},
                       err -> startRetryStrategy(reconnectStrategy));
    }

    private void startRetryStrategy(final Completable reconnectStrategy) {
        reconnectStrategy.subscribe(() -> applyStrategy(reconnectStrategy),
                                    err -> applyStrategy(reconnectStrategy));
    }
}
