package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.google.common.base.Supplier;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationUtilTest extends CommonUtilForTest {

    private AuthentificationUtil authentificationUtil;

    @Mock private IClient clientMock;
    private final Subject<ConnectionState, ConnectionState> connectionStateObs = PublishSubject.create();
    private final TestSubscriber<LoginState> loginStateSubscriber = new TestSubscriber<>();
    private final static String jnlpAddress = "http://jnlp.test.address";
    private final static String userName = "username";
    private final static String password = "password";
    private final static String pin = "1234";
    private LoginCredentials loginCredentials;
    private LoginCredentials loginCredentialsWithPin;

    @Before
    public void setUp() {
        initCommonTestFramework();
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                userName,
                                                password);
        loginCredentialsWithPin = new LoginCredentials(jnlpAddress,
                                                       userName,
                                                       password,
                                                       pin);

        authentificationUtil = new AuthentificationUtil(clientMock, connectionStateObs);
        authentificationUtil.loginStateObs().subscribe(loginStateSubscriber);
    }

    private Optional<Exception> login() {
        return authentificationUtil.login(loginCredentials);
    }

    private Optional<Exception> loginWithPin() {
        return authentificationUtil.login(loginCredentialsWithPin);
    }

    private void verifyConnectCall(final LoginCredentials loginCredentials,
                                   final int times) {
        try {
            if (loginCredentials.pin().isEmpty())
                verify(clientMock, times(times)).connect(jnlpAddress, userName, password);
            else
                verify(clientMock, times(times)).connect(jnlpAddress, userName, password, pin);
        } catch (final Exception e) {}

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

    private void assertLoginStateNotification(final LoginState loginState,
                                              final int eventIndex) {
        loginStateSubscriber.assertNoErrors();
        loginStateSubscriber.assertValueCount(eventIndex + 1);

        assertThat(loginStateSubscriber.getOnNextEvents().get(eventIndex),
                equalTo(loginState));
    }

    @Test
    public void testLoginStateAfterCreationIsLoggedOut() {
        assertThat(authentificationUtil.state(), equalTo(LoginState.LOGGED_OUT));
    }

    @Test
    public void testLogoutAfterCreationDoesNotCallDisconnect() {
        authentificationUtil.logout();

        verify(clientMock, times(0)).disconnect();
    }

    @Test
    public void testReconnectAfterCreationDoesNotCallReconnectOnClient() {
        authentificationUtil.reconnect();

        verify(clientMock, never()).reconnect();
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
    public void testLoginWithPinCallsClientWithPin() {
        final Optional<Exception> exceptionOpt = loginWithPin();

        verifyConnectCall(loginCredentialsWithPin, 1);
        assertFalse(exceptionOpt.isPresent());
    }

    public class AfterLogin {

        @Before
        public void setUp() {
            final Optional<Exception> exceptionOpt = login();

            assertFalse(exceptionOpt.isPresent());
        }

        @Test
        public void testLoginCallsClient() {
            verifyConnectCall(loginCredentials, 1);
        }

        @Test
        public void testLoginStateStillLoggedOut() {
            assertThat(authentificationUtil.state(), equalTo(LoginState.LOGGED_OUT));
        }

        @Test
        public void testNoLoginNotificationYet() {
            loginStateSubscriber.assertNoValues();
        }

        @Test
        public void testNoReconnectionPossibleYet() {
            authentificationUtil.reconnect();

            verify(clientMock, never()).reconnect();
        }

        @Test
        public void testNoLogoutPossibleYet() {
            authentificationUtil.logout();

            verify(clientMock, times(0)).disconnect();
        }

        public class AfterConnectedMessage {

            @Before
            public void setUp() {
                connectionStateObs.onNext(ConnectionState.CONNECTED);
            }

            @Test
            public void testLoginStateIsLoggedIN() {
                assertThat(authentificationUtil.state(), equalTo(LoginState.LOGGED_IN));
            }

            @Test
            public void testNotificationForLoginHappens() {
                assertLoginStateNotification(LoginState.LOGGED_IN, 0);
            }

            @Test
            public void testReconnectionIsNowPossible() {
                authentificationUtil.reconnect();

                verify(clientMock).reconnect();
            }

            public class AfterLogout {

                @Before
                public void setUp() {
                    authentificationUtil.logout();
                }

                @Test
                public void testDisconnectIsCalledOnClient() {
                    verify(clientMock).disconnect();
                }

                @Test
                public void testLoginStateIsLoggedOut() {
                    assertThat(authentificationUtil.state(), equalTo(LoginState.LOGGED_OUT));
                }

                @Test
                public void testNotificationForLogoutHappens() {
                    assertLoginStateNotification(LoginState.LOGGED_OUT, 1);
                }

                @Test
                public void testReconnectionIsNotPossible() {
                    authentificationUtil.reconnect();

                    verify(clientMock, never()).reconnect();
                }
            }

            public class AfterDisconnectedMessage {

                @Before
                public void setUp() {
                    connectionStateObs.onNext(ConnectionState.DISCONNECTED);
                }

                @Test
                public void testLoginStateIsStillLoggedIN() {
                    assertThat(authentificationUtil.state(), equalTo(LoginState.LOGGED_IN));
                }

                @Test
                public void testNoNotificationHappens() {
                    loginStateSubscriber.assertNoErrors();
                    loginStateSubscriber.assertValueCount(1);
                }

                @Test
                public void testReconnectionIsNowPossible() {
                    authentificationUtil.reconnect();

                    verify(clientMock).reconnect();
                }
            }
        }
    }
}
