package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class ConnectionKeeper {

    private final IClient client;
    private final Observable<ConnectionState> connectionStateObservable;
    private final Observable<LoginState> loginStateObservable;
    private Disposable connectionStateSubscription;
    private Disposable loginStateSubscription;
    private LoginState currentLoginState = LoginState.LOGGED_OUT;
    private boolean isStarted = false;

    private static final Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final IClient client,
                            final Observable<ConnectionState> connectionStateObservable,
                            final Observable<LoginState> loginStateObservable) {
        this.client = client;
        this.connectionStateObservable = connectionStateObservable;
        this.loginStateObservable = loginStateObservable;
    }

    public synchronized void start() {
        if (!isStarted) {
            connectionStateSubscription = connectionStateObservable.subscribe(this::handleConnectionState);
            loginStateSubscription = loginStateObservable.subscribe(this::handleLoginState);
            isStarted = true;
        }
    }

    private void handleConnectionState(final ConnectionState connectionState) {
        logger.debug("Received connection state update " + connectionState
                + ". Current login state is " + currentLoginState);
        if (isConnectionLostWhileLoggedIn(connectionState)) {
            logger.warn("Connection lost! Try to reconnect...");
            client.reconnect();
        }
    }

    private boolean isConnectionLostWhileLoggedIn(final ConnectionState connectionState) {
        return connectionState == ConnectionState.DISCONNECTED
                && currentLoginState == LoginState.LOGGED_IN;
    }

    private void handleLoginState(final LoginState loginState) {
        logger.debug("Received login state update " + loginState);
        this.currentLoginState = loginState;
    }

    public synchronized void stop() {
        if (isStarted) {
            connectionStateSubscription.dispose();
            loginStateSubscription.dispose();
            isStarted = false;
        }
    }
}
