package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.system.IClient;

import rx.Observable;

public class LoginHandler {

    private final Authentification authentification;
    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final IClient client) {
        this.client = client;
        authentification = new Authentification(client);
    }

    public void login(final String jnlp,
                      final String username,
                      final String password) {
        final LoginResult loginResult = authentification.login(jnlp, username, password);
        evaluateLoginResult(loginResult);
    }

    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password) {
        final String pin = new LivePinForm(client, jnlp).getPin();
        final LoginResult loginResult = authentification.loginWithPin(jnlp, username, password, pin);
        evaluateLoginResult(loginResult);
    }

    private void evaluateLoginResult(final LoginResult loginResult) {
        if (loginResult.loginResultType() == LoginResultType.LOGGED_IN)
            updateState(LoginState.LOGGED_IN);
        else
            logger.error("Exception occured during login! " + loginResult.loginExceptionOpt().get());
    }

    private void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public LoginState state() {
        return loginState;
    }

    public Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    public void logout() {
        authentification.logout();
        updateState(LoginState.LOGGED_OUT);
    }
}
