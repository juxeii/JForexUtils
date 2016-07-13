package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationUtilTest extends CommonUtilForTest {

    private AuthentificationUtil authentificationUtil;

    private final Subject<ConnectionState, ConnectionState> connectionStateObs =
            PublishSubject.create();
    private final TestSubscriber<LoginState> loginStateSubscriber = new TestSubscriber<>();
    private static final String jnlpAddress = "http://jnlp.test.address";
    private static final String userName = "username";
    private static final String password = "password";
    private static final String pin = "1234";
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

    private Completable login() {
        return authentificationUtil.loginCompletable(loginCredentials);
    }

    private Completable loginWithPin() {
        return authentificationUtil.loginCompletable(loginCredentialsWithPin);
    }

    private void
            assertLoginException(final Class<? extends Exception> exceptionType) throws Exception {
        assertLoginExceptionForLoginType(this::login, exceptionType, loginCredentials);
        assertLoginExceptionForLoginType(this::loginWithPin, exceptionType,
                                         loginCredentialsWithPin);
    }

    private void assertLoginExceptionForLoginType(final Supplier<Completable> loginCall,
                                                  final Class<? extends Exception> exceptionType,
                                                  final LoginCredentials loginCredentials) throws Exception {
        clientForTest.setExceptionOnConnect(loginCredentials, exceptionType);

        final TestSubscriber<?> loginSubscriber = new TestSubscriber<>();
        loginCall.get().subscribe(loginSubscriber);

        loginSubscriber.assertError(exceptionType);
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
        assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
    }

    @Test
    public void testLogoutAfterCreationDoesNotCallDisconnect() {
        authentificationUtil.logout();

        verify(clientMock, times(0)).disconnect();
    }

    @Test
    public void testCorrectExceptionForInvalidCredentials() throws Exception {
        assertLoginException(JFAuthenticationException.class);
    }

    @Test
    public void testCorrectExceptionForInvalidVersion() throws Exception {
        assertLoginException(JFVersionException.class);
    }

    @Test
    public void testCorrectExceptionForException() throws Exception {
        assertLoginException(Exception.class);
    }

    @Test
    public void testLoginWithPinCallsClientWithPin() throws Exception {
        loginWithPin().subscribe();

        clientForTest.verifyConnectCall(loginCredentialsWithPin, 1);
    }

    public class AfterLogin {

        protected final TestSubscriber<?> loginCompletionSubscriber = new TestSubscriber<>();

        @Before
        public void setUp() {
            login().subscribe(loginCompletionSubscriber);

            loginCompletionSubscriber.assertNoErrors();
        }

        @Test
        public void testLoginCallsClient() throws Exception {
            clientForTest.verifyConnectCall(loginCredentials, 1);
        }

        @Test
        public void testLoginStateStillLoggedOut() {
            assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
        }

        @Test
        public void testNoLogoutPossibleYet() {
            authentificationUtil.logout();

            verify(clientMock, times(0)).disconnect();
        }

        public class AfterDisconnectedMessage {

            @Before
            public void setUp() {
                connectionStateObs.onNext(ConnectionState.DISCONNECTED);
            }

            @Test
            public void testLoginStateIsStillLoggedOut() {
                assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
            }

            @Test
            public void testNoNotificationHappens() {
                loginStateSubscriber.assertNoErrors();
                loginStateSubscriber.assertValueCount(0);
            }
        }

        public class AfterConnectedMessage {

            @Before
            public void setUp() {
                connectionStateObs.onNext(ConnectionState.CONNECTED);
            }

            @Test
            public void testLoginStateIsLoggedIN() {
                assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_IN));
            }

            @Test
            public void testNotificationForLoginHappens() {
                assertLoginStateNotification(LoginState.LOGGED_IN, 0);
            }

            @Test
            public void testLoginCompletion() {
                loginCompletionSubscriber.assertCompleted();
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
                    assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
                }

                @Test
                public void testNotificationForLogoutHappens() {
                    assertLoginStateNotification(LoginState.LOGGED_OUT, 1);
                }
            }

            public class AfterDisconnectedMessage {

                @Before
                public void setUp() {
                    connectionStateObs.onNext(ConnectionState.DISCONNECTED);
                }

                @Test
                public void testLoginStateIsStillLoggedIN() {
                    assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_IN));
                }

                @Test
                public void testNoFurtherNotificationHappens() {
                    loginStateSubscriber.assertNoErrors();
                    loginStateSubscriber.assertValueCount(1);
                }
            }
        }
    }
}
