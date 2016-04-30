package com.jforex.programming.connection;

import java.util.Optional;

import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.system.IClient;

import rx.Observable;

public final class AuthentificationUtil {

    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private LoginState loginState = LoginState.LOGGED_OUT;

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;
        connectionStateObs.subscribe(this::onConnectionState);
    }

    public final Optional<Exception> login(final LoginCredentials loginCredentials) {
        return connectClient(loginCredentials.jnlpAddress(),
                             loginCredentials.username(),
                             loginCredentials.password(),
                             loginCredentials.pinOpt());
    }

    private final Optional<Exception> connectClient(final String jnlpAddress,
                                                    final String username,
                                                    final String password,
                                                    final Optional<String> pinOpt) {
        try {
            if (pinOpt.isPresent())
                client.connect(jnlpAddress, username, password, pinOpt.get());
            else
                client.connect(jnlpAddress, username, password);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final void logout() {
        if (isLoggedIn()) {
            client.disconnect();
            updateState(LoginState.LOGGED_OUT);
        }
    }

    private boolean isLoggedIn() {
        return state() == LoginState.LOGGED_IN;
    }

    public final Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    private final void onConnectionState(final ConnectionState connectionState) {
        if (isLoginTrigger(connectionState))
            updateState(LoginState.LOGGED_IN);
    }

    private boolean isLoginTrigger(final ConnectionState connectionState) {
        return connectionState == ConnectionState.CONNECTED &&
                loginState == LoginState.LOGGED_OUT;
    }

    private final synchronized void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public final LoginState state() {
        return loginState;
    }

    public final void reconnect() {
        if (isLoggedIn())
            client.reconnect();
    }
}
