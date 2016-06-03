package com.jforex.programming.test.fakes;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.test.common.CommonUtilForTest;

public class IClientForTest extends CommonUtilForTest {

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
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
    }
}
