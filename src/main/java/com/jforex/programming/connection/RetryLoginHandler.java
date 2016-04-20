package com.jforex.programming.connection;

import com.jforex.programming.misc.JFEventPublisherForRx;

import rx.Observable;

public class RetryLoginHandler implements LoginHandler {

    private final Authentification authentification;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private final LoginState loginState = LoginState.LOGGED_OUT;

    public RetryLoginHandler(final Authentification authentification) {
        this.authentification = authentification;
    }

    @Override
    public void loginDemo(final String jnlp,
                          final String username,
                          final String password) {
        authentification.loginDemo(jnlp, username, password);

    }

    @Override
    public void loginLive(final String jnlp,
                          final String username,
                          final String password,
                          final String pin) {
        authentification.loginLive(jnlp, username, password, pin);
    }

    public LoginState state() {
        return loginState;
    }

    @Override
    public Observable<LoginState> loginStateObs() {
        return (Observable<LoginState>) loginStatePublisher.observable();
    }
}
