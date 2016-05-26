package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

import rx.Observable;

public final class ConnectionKeeper {

    private final IClient client;
    private final AuthentificationUtil authentificationUtil;

    private final static Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final IClient client,
                            final Observable<ConnectionState> connectionStateObs,
                            final AuthentificationUtil authentificationUtil) {
        this.client = client;
        this.authentificationUtil = authentificationUtil;

        intObservables(connectionStateObs);
    }

    private final void intObservables(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(connectionState -> {
            logger.debug(connectionState + " message received!");
            if (connectionState == ConnectionState.CONNECTED || client.isConnected()) {
                logger.debug("Connection established");
            } else {
                if (authentificationUtil.loginState() == LoginState.LOGGED_IN) {
                    logger.debug("Try to do a light reconnection...");
                    client.reconnect();
                }
            }
        });
    }
}
