package com.jforex.programming.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.rx.JFHotPublisher;

import io.reactivex.Completable;

public class Authentification {

    private final IClient client;
    private final JFHotPublisher<LoginState> loginStatePublisher;

    public Authentification(final IClient client,
                            final JFHotPublisher<LoginState> loginStatePublisher) {
        this.client = client;
        this.loginStatePublisher = loginStatePublisher;
    }

    public Completable login(final LoginCredentials loginCredentials) {
        checkNotNull(loginCredentials);

        final String jnlpAddress = loginCredentials.jnlpAddress();
        final String username = loginCredentials.username();
        final String password = loginCredentials.password();
        final Optional<String> maybePin = loginCredentials.maybePin();

        return Completable
            .fromAction(maybePin.isPresent()
                    ? () -> client.connect(jnlpAddress,
                                           username,
                                           password,
                                           maybePin.get())
                    : () -> client.connect(jnlpAddress,
                                           username,
                                           password))
            .doOnComplete(() -> loginStatePublisher.onNext(LoginState.LOGGED_IN));
    }

    public Completable logout() {
        return Completable
            .fromAction(client::disconnect)
            .doOnComplete(() -> loginStatePublisher.onNext(LoginState.LOGGED_OUT));
    }
}
