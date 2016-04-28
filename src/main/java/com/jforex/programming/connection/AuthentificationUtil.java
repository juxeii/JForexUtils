package com.jforex.programming.connection;

import java.util.Optional;

import com.google.common.base.Supplier;
import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;

import rx.Observable;

public class AuthentificationUtil {

    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private Supplier<Optional<Exception>> latestLoginCall;
    private LoginState loginState = LoginState.LOGGED_OUT;

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;
        connectionStateObs.subscribe(this::onConnectionState);
    }

    public final synchronized Optional<Exception> login(final String jnlpAddress,
                                                        final String username,
                                                        final String password) {
        latestLoginCall = () -> connectClient(jnlpAddress, username, password, "");
        return latestLoginCall.get();
    }

    public final synchronized Optional<Exception> loginWithPin(final String jnlpAddress,
                                                               final String username,
                                                               final String password,
                                                               final String pin) {
        latestLoginCall = () -> connectClient(jnlpAddress, username, password, pin);
        return latestLoginCall.get();
    }

    private Optional<Exception> connectClient(final String jnlpAddress,
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

    public void logout() {
        if (state() == LoginState.LOGGED_IN) {
            client.disconnect();
            updateState(LoginState.LOGGED_OUT);
        }
    }

    public Optional<Exception> relogin() {
        return latestLoginCall != null
                ? latestLoginCall.get()
                : Optional.of(new JFException("Failed to relogin since client was not logged in before!"));
    }

    public Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    private void onConnectionState(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED && loginState == LoginState.LOGGED_OUT)
            updateState(LoginState.LOGGED_IN);
    }

    private void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public LoginState state() {
        return loginState;
    }

    public void reconnect() {
        if (state() == LoginState.LOGGED_IN)
            client.reconnect();
    }
}
