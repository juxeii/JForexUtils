package com.jforex.programming.connection;

import com.jforex.programming.misc.JFEventPublisherForRx;

import rx.Observable;

public class ConnectionMonitor {

    private final JFEventPublisherForRx<ConnectionState> connectionStatePublisher = new JFEventPublisherForRx<>();
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public ConnectionMonitor(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(this::onConnectionStateUpdate);
    }

    private void onConnectionStateUpdate(final ConnectionState connectionState) {
        System.out.println("CONNECTION MESSAGE " + connectionState);
        updateState(connectionState);
    }

    public ConnectionState state() {
        return connectionState;
    }

    public Observable<ConnectionState> connectionObs() {
        return connectionStatePublisher.observable();
    }

    private void updateState(final ConnectionState connectionState) {
        this.connectionState = connectionState;
        connectionStatePublisher.onJFEvent(connectionState);
    }
}
