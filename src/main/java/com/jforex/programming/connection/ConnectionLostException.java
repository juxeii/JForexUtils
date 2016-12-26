package com.jforex.programming.connection;

public final class ConnectionLostException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectionLostException(final String message) {
        super(message);
    }
}
