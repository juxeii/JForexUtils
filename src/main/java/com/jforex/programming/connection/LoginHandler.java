package com.jforex.programming.connection;

import rx.Observable;

public interface LoginHandler {

    public void login(final String jnlp,
                      final String username,
                      final String password);

    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password);

    public void logout();

    public LoginState state();

    public Observable<LoginState> loginStateObs();
}
