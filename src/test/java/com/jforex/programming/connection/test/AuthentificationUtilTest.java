package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.google.common.base.Supplier;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class AuthentificationUtilTest extends CommonUtilForTest {

    private AuthentificationUtil authentificationUtil;

    @Mock private IClient clientMock;
    private final Subject<ConnectionState, ConnectionState> connectionStateObs = PublishSubject.create();
    private final TestSubscriber<LoginState> loginStateSubscriber = new TestSubscriber<>();
    private final static String jnlpAddress = "http://jnlp.test.address";
    private final static String userName = "username";
    private final static String password = "password";
    private final static String pin = "1234";

    @Before
    public void setUp() {
        initCommonTestFramework();

        authentificationUtil = new AuthentificationUtil(clientMock, connectionStateObs);
        authentificationUtil.loginStateObs().subscribe(loginStateSubscriber);
    }

    private Optional<Exception> login() {
        return authentificationUtil.login(jnlpAddress, userName, password);
    }

    private Optional<Exception> loginWithPin() {
        return authentificationUtil.loginWithPin(jnlpAddress, userName, password, pin);
    }

    private void setExceptionOnConnect(final Class<? extends Exception> exceptionType) {
        try {
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password);
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password, pin);
        } catch (final Exception e) {}
    }

    private void assertLoginException(final Class<? extends Exception> exceptionType) {
        assertLoginExceptionForLoginType(this::login, exceptionType);
        assertLoginExceptionForLoginType(this::loginWithPin, exceptionType);
    }

    private void assertLoginExceptionForLoginType(final Supplier<Optional<Exception>> loginCall,
                                                  final Class<? extends Exception> exceptionType) {
        setExceptionOnConnect(exceptionType);

        final Optional<Exception> exceptionOpt = loginCall.get();

        assertThat(exceptionOpt.get().getClass(), equalTo(exceptionType));
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
        authentificationUtil.logout();

        verify(clientMock).disconnect();
    }
}
