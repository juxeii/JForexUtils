package com.jforex.programming.connection;

import com.jforex.programming.misc.JFEventPublisherForRx;

import rx.Observable;

public class ConnectionMonitor {

    private final JFEventPublisherForRx<ConnectionState> connectionStatePublisher = new JFEventPublisherForRx<>();
    private ConnectionState connectionState = ConnectionState.LOGGED_OUT;

    public void onLogin() {
        updateState(ConnectionState.LOGGED_IN);
    }

    public void onLogout() {
        updateState(ConnectionState.LOGGED_OUT);
    }

    public void onDisconnect() {
        updateState(ConnectionState.DISCONNECTED);
    }

    private void updateState(final ConnectionState connectionState) {
        this.connectionState = connectionState;
        connectionStatePublisher.onJFEvent(connectionState);
    }

    public ConnectionState state() {
        return connectionState;
    }

    public Observable<ConnectionState> connectionObs() {
        return (Observable<ConnectionState>) connectionStatePublisher.observable();
    }
}
