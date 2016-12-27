package com.jforex.programming.connection;

import com.dukascopy.api.system.IClient;

import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;

public class Reconnector {

    private final IClient client;
    private final Authentification authentification;
    private final UserConnection userConnection;
    private Completable lightReconnector;
    private Completable loginReconnector;

    public Reconnector(final IClient client,
                       final Authentification authentification,
                       final UserConnection userConnection) {
        this.client = client;
        this.authentification = authentification;
        this.userConnection = userConnection;

        lightReconnector = Completable.error(connectionLostException());
        loginReconnector = lightReconnector;
        monitorConnection();
    }

    private ConnectionLostException connectionLostException() {
        return new ConnectionLostException("Connection to server lost while logged in");
    }

    public void composeLightReconnect(final CompletableTransformer transformer) {
        lightReconnector = Completable
            .fromAction(client::reconnect)
            .andThen(userCompletableWithError(transformer));
    }

    public void composeLoginReconnect(final LoginCredentials loginCredentials,
                                      final CompletableTransformer transformer) {
        loginReconnector = authentification
            .login(loginCredentials)
            .andThen(userCompletableWithError(transformer));
    }

    private Completable userCompletableWithError(final CompletableTransformer transformer) {
        return observeUserConnectionStateWithError()
            .takeUntil(this::isConnected)
            .ignoreElements()
            .compose(transformer);
    }

    private void monitorConnection() {
        userConnection
            .observe()
            .takeUntil(this::isDisconnected)
            .subscribe(userConnectionState -> {
                if (isDisconnected(userConnectionState))
                    startRetryStrategy();
            });
    }

    private boolean isConnected(final UserConnectionState userConnectionState) {
        return userConnectionState == UserConnectionState.CONNECTED;
    }

    private boolean isDisconnected(final UserConnectionState userConnectionState) {
        return userConnectionState == UserConnectionState.DISCONNECTED;
    }

    private void startRetryStrategy() {
        lightReconnector
            .onErrorResumeNext(err -> loginReconnector)
            .doAfterTerminate(() -> monitorConnection())
            .subscribe(() -> {}, err -> {});
    }

    private final Observable<UserConnectionState> observeUserConnectionStateWithError() {
        return userConnection
            .observe()
            .concatMap(userConnectionState -> isDisconnected(userConnectionState)
                    ? Observable.error(connectionLostException())
                    : Observable.just(userConnectionState));
    }
}
