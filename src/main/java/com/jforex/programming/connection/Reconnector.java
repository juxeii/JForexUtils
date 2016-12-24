package com.jforex.programming.connection;

import io.reactivex.Completable;

public class Reconnector {

    private final ConnectionMonitor connectionMonitor;

    public Reconnector(final ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
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
