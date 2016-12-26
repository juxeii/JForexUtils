package com.jforex.programming.connection;

import com.dukascopy.api.system.IClient;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class Reconnector {

    private final IClient client;
    private final Authentification authentification;
    private final UserConnection userConnection;

    public Reconnector(final IClient client,
                       final Authentification authentification,
                       final UserConnection userConnection) {
        this.client = client;
        this.authentification = authentification;
        this.userConnection = userConnection;
    }

    public Completable lightReconnect() {
        return Completable
            .fromAction(client::reconnect)
            .andThen(userCompletableWithError());
    }

    public Completable relogin(final LoginCredentials loginCredentials) {
        return authentification
            .login(loginCredentials)
            .andThen(userCompletableWithError());
    }

    private Completable userCompletableWithError() {
        return observeUserConnectionStateWithError()
            .takeUntil(userConnectionState -> userConnectionState == UserConnectionState.CONNECTED)
            .ignoreElements();
    }

    public void applyStrategy(final Completable retryStrategy) {
        userConnection
            .observe()
            .takeUntil(this::isDisconnected)
            .subscribe(userConnectionState -> {
                if (isDisconnected(userConnectionState))
                    startRetryStrategy(retryStrategy);
            });
    }

    private boolean isDisconnected(final UserConnectionState userConnectionState) {
        return userConnectionState == UserConnectionState.DISCONNECTED;
    }

    private void startRetryStrategy(final Completable retryStrategy) {
        retryStrategy
            .doOnSubscribe(d -> System.out.println("Trying to reconnect..."))
            .doAfterTerminate(() -> applyStrategy(retryStrategy))
            .subscribe(() -> {
                System.out.println("Connection successfully reestablished.");
            }, err -> {
                System.out.println("Failed to reconnect! " + err.getMessage());
            });
    }

    private final Observable<UserConnectionState> observeUserConnectionStateWithError() {
        return userConnection
            .observe()
            .concatMap(userConnectionState -> isDisconnected(userConnectionState)
                    ? Observable.error(new ConnectionLostException("Connection to server lost while logged in"))
                    : Observable.just(userConnectionState));
    }
}
