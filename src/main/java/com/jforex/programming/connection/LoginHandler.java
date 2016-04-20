package com.jforex.programming.connection;

import rx.Observable;

public interface LoginHandler {

    public void loginDemo(final String jnlp,
                          final String username,
                          final String password);

    public void loginLive(final String jnlp,
                          final String username,
                          final String password,
                          final String pin);

    public Observable<LoginState> loginStateObs();
}
