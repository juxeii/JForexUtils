package com.jforex.programming.connection;

import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.IMessage;

import rx.Observable;

public class ConnectionMonitor {

    private final JFEventPublisherForRx<ConnectionState> connectionStatePublisher = new JFEventPublisherForRx<>();
    private ConnectionState connectionState = ConnectionState.LOGGED_OUT;

    public ConnectionMonitor(final Observable<LoginState> loginStateObs,
                             final Observable<IMessage> messageObservable) {
        loginStateObs.subscribe(this::onLoginStateUpdate);
        messageObservable.filter(message -> message.getType() == IMessage.Type.CONNECTION_STATUS)
                         .subscribe(this::onConnectionMessage);
    }

    private void onLoginStateUpdate(final LoginState loginState) {
        System.out.println("LOGIN UPDATE " + loginState);
        if (loginState == LoginState.LOGGED_IN)
            updateState(ConnectionState.LOGGED_IN);
        else
            updateState(ConnectionState.LOGGED_OUT);
    }

    private void onConnectionMessage(final IMessage connectionMessage) {
        final String connectionMessageContent = connectionMessage.getContent();
        System.out.println("CONNECTION MESSAGE " + connectionMessageContent);
        if (connectionMessageContent.equals("connect"))
            updateState(ConnectionState.LOGGED_IN);
        else if (connectionMessageContent.equals("disconnect"))
            updateState(ConnectionState.DISCONNECTED);
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
