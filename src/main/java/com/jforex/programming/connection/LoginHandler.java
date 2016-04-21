package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.google.common.base.Supplier;
import com.jforex.programming.misc.JFEventPublisherForRx;

import rx.Observable;

public class LoginHandler {

    private final Authentification authentification;
    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private Supplier<LoginResult> latestLoginCall;
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final IClient client) {
        this.client = client;
        authentification = new Authentification(client);
    }

    public void login(final String jnlp,
                      final String username,
                      final String password) {
        latestLoginCall = () -> authentification.login(jnlp, username, password);
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password) {
        latestLoginCall = () -> {
            final String pin = new LivePinForm(client, jnlp).getPin();
            return authentification.login(jnlp, username, password);
        };
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void relogin() {
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void reconnect() {
        client.reconnect();
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
