package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectException;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationUtilTest extends CommonUtilForTest {

    private AuthentificationUtil authentificationUtil;

    private final Subject<ConnectionState, ConnectionState> connectionStateObs = PublishSubject.create();
    private final TestSubscriber<LoginState> loginStateSubscriber = new TestSubscriber<>();
    private final TestScheduler scheduler = new TestScheduler();
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

    private Completable login() {
        return authentificationUtil.login(loginCredentials);
    }

    private Completable loginWithPin() {
        return authentificationUtil.login(loginCredentialsWithPin);
    }

    private void assertLoginException(final Class<? extends Exception> exceptionType) {
        assertLoginExceptionForLoginType(this::login, exceptionType);
        assertLoginExceptionForLoginType(this::loginWithPin, exceptionType);
    }

    private void assertLoginExceptionForLoginType(final Supplier<Completable> loginCall,
                                                  final Class<? extends Exception> exceptionType) {
        clientForTest.setExceptionOnConnect(loginCredentials, exceptionType);
        clientForTest.setExceptionOnConnectWithPin(loginCredentialsWithPin, exceptionType);

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
        loginWithPin();

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
        public void testLoginCallsClient() {
            clientForTest.verifyConnectCall(loginCredentials, 1);
        }

        @Test
        public void testLoginStateStillLoggedOut() {
            assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
        }

        @Test
        public void testNoLoginCompletionYet() {
            loginCompletionSubscriber.assertNotCompleted();
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

            @Test
            public void testLoginCompletionError() {
                loginCompletionSubscriber.assertError(ConnectException.class);
            }
        }

        public class AfterLoginTimeOut {

            @Before
            public void setUp() {
                scheduler.advanceTimeBy(platformSettings.logintimeoutseconds(), TimeUnit.SECONDS);
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

            @Test
            public void testLoginCompletionError() {
                rxTestUtil.advanceTimeBy(20, TimeUnit.SECONDS);
                loginCompletionSubscriber.assertError(TimeoutException.class);
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
