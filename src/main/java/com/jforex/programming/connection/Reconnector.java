package com.jforex.programming.connection;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.rx.UserConnectionStateTransformer;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class Reconnector {

    private final IClient client;
    private final Authentification authentification;
    private final UserConnection userConnection;
    private Completable lightReconnector;
    private Completable loginReconnector;
    private Completable reconnectStrategy = Completable.complete();

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

    public void composeLightReconnect(final UserConnectionStateTransformer transformer) {
        lightReconnector = Completable
            .fromAction(client::reconnect)
            .andThen(userCompletableWithError(transformer));
        composeReconnectStrategy();
    }

    public void composeLoginReconnect(final LoginCredentials loginCredentials,
                                      final UserConnectionStateTransformer transformer) {
        loginReconnector = authentification
            .login(loginCredentials)
            .andThen(userCompletableWithError(transformer));
        composeReconnectStrategy();
    }

    private Completable userCompletableWithError(final UserConnectionStateTransformer transformer) {
        return observeUserConnectionStateWithError()
            .takeUntil(this::isConnected)
            .compose(transformer)
            .ignoreElements();
    }

    private void composeReconnectStrategy() {
        reconnectStrategy = lightReconnector
            .onErrorResumeNext(err -> loginReconnector)
            .doAfterTerminate(this::monitorConnection);
    }

    private void monitorConnection() {
        userConnection
            .observe()
            .takeUntil(this::isDisconnected)
            .doOnComplete(this::startRetryStrategy)
            .subscribe();
    }

    private boolean isConnected(final UserConnectionState userConnectionState) {
        return userConnectionState == UserConnectionState.CONNECTED;
    }

    private boolean isDisconnected(final UserConnectionState userConnectionState) {
        return userConnectionState == UserConnectionState.DISCONNECTED;
    }

    private void startRetryStrategy() {
        reconnectStrategy.subscribe(() -> {}, err -> {});
    }

    private final Observable<UserConnectionState> observeUserConnectionStateWithError() {
        return userConnection
            .observe()
            .concatMap(userConnectionState -> isDisconnected(userConnectionState)
                    ? Observable.error(connectionLostException())
                    : Observable.just(userConnectionState));
    }
}
