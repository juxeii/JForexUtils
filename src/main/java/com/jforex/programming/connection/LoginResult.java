package com.jforex.programming.connection;

import java.util.Optional;

public final class LoginResult {

    private final LoginResultType loginResultType;
    private final Optional<Exception> loginExceptionOpt;

    public LoginResult(final LoginResultType loginResultType,
                                  final Optional<Exception> loginExceptionOpt) {
        this.loginResultType = loginResultType;
        this.loginExceptionOpt = loginExceptionOpt;
    }

    public final LoginResultType loginResultType() {
        return loginResultType;
    }

    public final Optional<Exception> loginExceptionOpt() {
        return loginExceptionOpt;
    }
}
