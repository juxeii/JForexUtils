package com.jforex.programming.test.fakes;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.LoginCredentials;

public class IClientForTest {

    private final IClient clientMock;

    public IClientForTest(final IClient clientMock) {
        this.clientMock = clientMock;
    }

    public void verifyConnectCall(final LoginCredentials loginCredentials,
                                  final int times) {
        try {
            if (loginCredentials.maybePin().isPresent())
                verify(clientMock, times(times)).connect(loginCredentials.jnlpAddress(),
                                                         loginCredentials.username(),
                                                         loginCredentials.password(),
                                                         loginCredentials.maybePin().get());
            else
                verify(clientMock, times(times)).connect(loginCredentials.jnlpAddress(),
                                                         loginCredentials.username(),
                                                         loginCredentials.password());
        } catch (final Exception e) {}
    }

    public void setExceptionOnConnect(final LoginCredentials loginCredentials,
                                      final Class<? extends Exception> exceptionType) {
        try {
            if (loginCredentials.maybePin().isPresent())
                doThrow(exceptionType).when(clientMock).connect(loginCredentials.jnlpAddress(),
                                                                loginCredentials.username(),
                                                                loginCredentials.password(),
                                                                loginCredentials.maybePin().get());
            else
                doThrow(exceptionType).when(clientMock).connect(loginCredentials.jnlpAddress(),
                                                                loginCredentials.username(),
                                                                loginCredentials.password());
        } catch (final Exception e) {}
    }
}
