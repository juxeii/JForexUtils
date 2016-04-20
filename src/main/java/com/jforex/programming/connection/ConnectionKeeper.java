package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

import rx.Observable;

public class ConnectionKeeper {

    private final IClient client;
    private Runnable reloginCall;
    private int noOfLightReconnects;
    private final LoginHandler loginHandler;

    private final static Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final IClient client,
                            final Runnable reloginCall,
                            final int noOfLightReconnects,
                            final Observable<ConnectionState> connectionObs,
                            final LoginHandler loginHandler) {
        this.client = client;
        this.noOfLightReconnects = noOfLightReconnects;
        this.loginHandler = loginHandler;
        connectionObs.subscribe(this::onConnectionStateUpdate);
        resetReconnectData();
    }

    private void resetReconnectData() {
        noOfLightReconnects = 3;
    }

    private void onConnectionStateUpdate(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED && loginHandler.state() == LoginState.LOGGED_IN) {
            logger.warn("Disconnect message received, starting reconnect strategy...");
            startReconnectStrategy();
        } else if (connectionState == ConnectionState.CONNECTED)
            resetReconnectData();
    }

    private void startReconnectStrategy() {
        if (noOfLightReconnects > 0) {
            logger.debug("Try to do a light reconnect.Remaining attempts " + noOfLightReconnects);
            doLightReconnect();
            --noOfLightReconnects;
        } else {
            while (!client.isConnected()) {
                relogin();
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e) {
                    logger.error("Exception while waiting for relogin! " + e.getMessage());
                }
            }
        }
    }

    private void doLightReconnect() {
        client.reconnect();
    }

    private void relogin() {
        logger.debug("Try to relogin...");
        reloginCall.run();
    }
}
