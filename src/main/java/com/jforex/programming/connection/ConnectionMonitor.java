package com.jforex.programming.connection;

import io.reactivex.Observable;

public class ConnectionMonitor {

    private final Observable<ConnectionState> connectionStateObservable;
    private LoginState currentLoginState = LoginState.LOGGED_OUT;

    public ConnectionMonitor(final Observable<ConnectionState> connectionStateObservable,
                             final Observable<LoginState> loginStateObservable) {
        this.connectionStateObservable = connectionStateObservable;

        loginStateObservable.subscribe(ls -> currentLoginState = ls);
    }

    public Observable<ConnectionState> observe() {
        return connectionStateObservable
            .concatMap(connectionState -> isConnectionLost(connectionState)
                    ? Observable.error(new ConnectionLostException())
                    : Observable.just(connectionState));
    }

    private boolean isConnectionLost(final ConnectionState connectionState) {
        return connectionState == ConnectionState.DISCONNECTED
                && currentLoginState == LoginState.LOGGED_IN;
    }
}
