package com.jforex.programming.connection;

import java.util.Optional;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.misc.JFEventPublisherForRx;

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
                loginCredentials.pin());
    }

    private final Optional<Exception> connectClient(final String jnlpAddress,
                                                    final String username,
                                                    final String password,
                                                    final String pin) {
        try {
            if (pin.isEmpty())
                client.connect(jnlpAddress, username, password);
            else
                client.connect(jnlpAddress, username, password, pin);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final void logout() {
        if (state() == LoginState.LOGGED_IN) {
            client.disconnect();
            updateState(LoginState.LOGGED_OUT);
        }
    }

    public final Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    private final void onConnectionState(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED && loginState == LoginState.LOGGED_OUT)
            updateState(LoginState.LOGGED_IN);
    }

    private final synchronized void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public final LoginState state() {
        return loginState;
    }

    public final void reconnect() {
        if (state() == LoginState.LOGGED_IN)
            client.reconnect();
    }
}
