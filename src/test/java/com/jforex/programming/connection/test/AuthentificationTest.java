package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.base.Supplier;
import com.jforex.programming.connection.Login;
import com.jforex.programming.test.common.CommonUtilForTest;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public class AuthentificationTest extends CommonUtilForTest {

    private Login authentification;

    @Mock
    private IClient clientMock;
    private final static String jnlpAddress = "http://jnlp.test.address";
    private final static String userName = "username";
    private final static String password = "password";
    private final static String pin = "1234";

    @Before
    public void setUp() {
        initCommonTestFramework();

        authentification = new Login(clientMock);
    }

    private Optional<Exception> login() {
        return authentification.withoutPin(jnlpAddress, userName, password);
    }

    private Optional<Exception> loginWithPin() {
        return authentification.withPin(jnlpAddress, userName, password, pin);
    }

    private void setExceptionOnConnect(final Class<? extends Exception> exceptionType) {
        try {
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password);
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password, pin);
        } catch (final Exception e) {}
    }

    private void assertLoginExceptionWithCallParameter(final Supplier<Optional<Exception>> loginCall,
                                                       final Class<? extends Exception> exceptionType) {
        setExceptionOnConnect(exceptionType);

        final Optional<Exception> exceptionOpt = loginCall.get();

        assertThat(exceptionOpt.get().getClass(), equalTo(exceptionType));
    }

    private void assertLoginException(final Class<? extends Exception> exceptionType) {
        assertLoginExceptionWithCallParameter(this::login, exceptionType);
        assertLoginExceptionWithCallParameter(this::loginWithPin, exceptionType);
    }

    @Test
    public void testLoginCallsClient() throws JFAuthenticationException,
                                       JFVersionException,
                                       Exception {
        login();

        verify(clientMock).connect(jnlpAddress, userName, password);
    }

    @Test
    public void testLoginWithPinCallsClient() throws JFAuthenticationException,
                                              JFVersionException,
                                              Exception {
        loginWithPin();

        verify(clientMock).connect(jnlpAddress, userName, password, pin);
    }

    @Test
    public void testCorrectExceptionForInvalidCredentials() {
        assertLoginException(JFAuthenticationException.class);
    }

    @Test
    public void testCorrectExceptionForInvalidVersion() {
        assertLoginException(JFVersionException.class);
    }

    @Test
    public void testCorrectExceptionForException() {
        assertLoginException(Exception.class);
    }

    @Test
    public void testOnLogoutDisconnectIsCalledOnClient() {
        authentification.logout();

        verify(clientMock).disconnect();
    }
}
