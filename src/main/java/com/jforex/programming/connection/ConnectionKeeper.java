package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;

public final class ConnectionKeeper {

    private final AuthentificationUtil authentificationUtil;
    private final LoginCredentials loginCredentials;
    private int noOfLightReconnects;

    private final static Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final Observable<ConnectionState> connectionObs,
                            final AuthentificationUtil loginHandler,
                            final LoginCredentials loginCredentials) {
        this.authentificationUtil = loginHandler;
        this.loginCredentials = loginCredentials;
        connectionObs.subscribe(this::onConnectionStateUpdate);
        resetReconnectData();
    }

    private final void resetReconnectData() {
        noOfLightReconnects = 3;
    }

    private final void onConnectionStateUpdate(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED &&
                authentificationUtil.state() == LoginState.LOGGED_IN) {
            logger.warn("Disconnect message received, starting reconnect strategy...");
            startReconnectStrategy();
        } else if (connectionState == ConnectionState.CONNECTED)
            resetReconnectData();
    }

    private final void startReconnectStrategy() {
        if (noOfLightReconnects > 0)
            doLightReconnect();
        else
            relogin();
    }

    private final void doLightReconnect() {
        logger.debug("Try to do a light reconnect.Remaining attempts " + noOfLightReconnects);
        authentificationUtil.reconnect();
        --noOfLightReconnects;
    }

    private final void relogin() {
        logger.debug("Try to relogin...");
        authentificationUtil.login(loginCredentials);
    }
}
