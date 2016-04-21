package com.jforex.programming.connection;

import java.util.Optional;

public final class LoginResult {

    private final LoginResultType type;
    private final Optional<Exception> exceptionOpt;

    public LoginResult(final LoginResultType type,
                       final Optional<Exception> exceptionOpt) {
        this.type = type;
        this.exceptionOpt = exceptionOpt;
    }

    public final LoginResultType type() {
        return type;
    }

    public final Optional<Exception> exceptionOpt() {
        return exceptionOpt;
    }
}
