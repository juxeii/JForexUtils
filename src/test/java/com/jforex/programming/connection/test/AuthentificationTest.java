package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.google.common.base.Supplier;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginResult;
import com.jforex.programming.connection.LoginResultType;
import com.jforex.programming.test.common.CommonUtilForTest;

public class AuthentificationTest extends CommonUtilForTest {

    private Authentification authentification;

    @Mock private IClient clientMock;
    private final static String jnlpAddress = "http://jnlp.test.address";
    private final static String userName = "username";
    private final static String password = "password";
    private final static String pin = "1234";

    @Before
    public void setUp() {
        initCommonTestFramework();

        authentification = new Authentification(clientMock);
    }

    private LoginResult loginDemo() {
        return authentification.login(jnlpAddress, userName, password);
    }

    private LoginResult loginLive() {
        return authentification.loginWithPin(jnlpAddress, userName, password, pin);
    }

    private void setExceptionOnConnect(final Class<? extends Exception> exceptionType) {
        try {
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password);
            doThrow(exceptionType).when(clientMock).connect(jnlpAddress, userName, password, pin);
        } catch (final Exception e) {}
    }

    private void assertResultContents(final LoginResult result,
                                      final LoginResultType loginResultType,
                                      final Class<? extends Exception> exceptionType) {
        assertThat(result.type(), equalTo(loginResultType));
        assertTrue(exceptionType.isInstance(result.exceptionOpt().get()));
    }

    private void assertLoginException(final LoginResultType loginResultType,
                                      final Class<? extends Exception> exceptionType) {
        assertLoginExceptionWithCallParameter(this::loginDemo, loginResultType, exceptionType);
        assertLoginExceptionWithCallParameter(this::loginLive, loginResultType, exceptionType);
    }

    private void assertLoginExceptionWithCallParameter(final Supplier<LoginResult> loginCall,
                                                       final LoginResultType loginResultType,
                                                       final Class<? extends Exception> exceptionType) {
        setExceptionOnConnect(exceptionType);

        final LoginResult result = loginCall.get();

        assertResultContents(result, loginResultType, exceptionType);
    }

    @Test
    public void testOnDemoLoginConnectIsCalledOnClient() throws JFAuthenticationException,
                                                         JFVersionException,
                                                         Exception {
        loginDemo();

        verify(clientMock).connect(jnlpAddress, userName, password);
    }

    @Test
    public void testOnLiveLoginConnectIsCalledOnClient() throws JFAuthenticationException,
                                                         JFVersionException,
                                                         Exception {
        loginLive();

        verify(clientMock).connect(jnlpAddress, userName, password, pin);
    }

    @Test
    public void testLoginResultIsCorrectForInvalidCredentials() {
        assertLoginException(LoginResultType.INVALID_CREDENTIALS, JFAuthenticationException.class);
    }

    @Test
    public void testLoginResultIsCorrectForInvalidVersion() {
        assertLoginException(LoginResultType.INVALID_VERSION, JFVersionException.class);
    }

    @Test
    public void testLoginResultIsCorrectForException() {
        assertLoginException(LoginResultType.EXCEPTION, Exception.class);
    }

    @Test
    public void testOnLogoutDisconnectIsCalledOnClient() {
        authentification.logout();

        verify(clientMock).disconnect();
    }
}
