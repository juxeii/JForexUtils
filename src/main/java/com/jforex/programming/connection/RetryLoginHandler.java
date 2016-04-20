package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.system.IClient;

import rx.Observable;

public class RetryLoginHandler implements LoginHandler {

    private final Authentification authentification;
    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(RetryLoginHandler.class);

    public RetryLoginHandler(final Authentification authentification,
                             final IClient client) {
        this.authentification = authentification;
        this.client = client;
    }

    @Override
    public void login(final String jnlp,
                      final String username,
                      final String password) {
        final LoginResult loginResult = authentification.login(jnlp, username, password);
        if (loginResult.loginResultType() == LoginResultType.LOGGED_IN)
            waitForConnect();
    }

    @Override
    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password) {
        final String pin = new LivePinForm(client, jnlp).getPin();
        final LoginResult loginResult = authentification.loginWithPin(jnlp, username, password, pin);
        if (loginResult.loginResultType() == LoginResultType.LOGGED_IN)
            waitForConnect();
    }

    private void waitForConnect() {
        try {
            while (!client.isConnected()) {
                logger.debug("Client not connected, waiting...");
                Thread.sleep(1000L);
            }
            updateState(LoginState.LOGGED_IN);
        } catch (final InterruptedException e) {
            logger.error("Error while waiting for login!" + e.getMessage());
        }
    }

    private void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    @Override
    public LoginState state() {
        return loginState;
    }

    @Override
    public Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    @Override
    public void logout() {
        if (!client.isConnected()) {
            client.disconnect();
            updateState(LoginState.LOGGED_OUT);
        }
    }
}
