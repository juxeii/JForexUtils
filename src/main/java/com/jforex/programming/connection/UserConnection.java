package com.jforex.programming.connection;

import io.reactivex.Observable;

public class UserConnection {

    private final Observable<ConnectionState> connectionStateObservable;
    private LoginState currentLoginState = LoginState.LOGGED_OUT;

    public UserConnection(final Observable<ConnectionState> connectionStateObservable,
                          final Observable<LoginState> loginStateObservable) {
        this.connectionStateObservable = connectionStateObservable;

        loginStateObservable.subscribe(ls -> currentLoginState = ls);
    }

    public Observable<UserConnectionState> observe() {
        return connectionStateObservable.map(this::mapConnectionState);
    }

    private UserConnectionState mapConnectionState(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED)
            return UserConnectionState.CONNECTED;
        if (connectionState == ConnectionState.DISCONNECTED && currentLoginState == LoginState.LOGGED_IN)
            return UserConnectionState.DISCONNECTED;
        return UserConnectionState.LOGGED_OUT;
    }
}
