package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationUtilTest extends CommonUtilForTest {

    private AuthentificationUtil authentificationUtil;

    @Mock
    private TaskExecutor taskExecutorMock;
    @Captor
    private ArgumentCaptor<Action> loginActionCaptor;
    private final Subject<ConnectionState> connectionStateObs = PublishSubject.create();
    private final TestObserver<LoginState> loginStateSubscriber = TestObserver.create();

    @Before
    public void setUp() {
        authentificationUtil = new AuthentificationUtil(clientMock,
                                                        taskExecutorMock,
                                                        connectionStateObs);

        authentificationUtil
            .observeLoginState()
            .subscribe(loginStateSubscriber);
    }

    private void verifyConnectCall(final LoginCredentials loginCredentials,
                                   final int times) throws Exception {
        verify(taskExecutorMock).onCurrentThread(loginActionCaptor.capture());
        loginActionCaptor.getValue().run();

        if (loginCredentials.maybePin().isPresent())
            verify(clientMock, times(times)).connect(loginCredentials.jnlpAddress(),
                                                     loginCredentials.username(),
                                                     loginCredentials.password(),
                                                     loginCredentials.maybePin().get());
        else
            verify(clientMock, times(times)).connect(loginCredentials.jnlpAddress(),
                                                     loginCredentials.username(),
                                                     loginCredentials.password());
    }

    private void setExceptionOnConnect(final LoginCredentials loginCredentials,
                                       final Class<? extends Exception> exceptionType) throws Exception {
        if (loginCredentials.maybePin().isPresent())
            doThrow(exceptionType).when(clientMock).connect(loginCredentials.jnlpAddress(),
                                                            loginCredentials.username(),
                                                            loginCredentials.password(),
                                                            loginCredentials.maybePin().get());
        else
            doThrow(exceptionType).when(clientMock).connect(loginCredentials.jnlpAddress(),
                                                            loginCredentials.username(),
                                                            loginCredentials.password());
    }

    private Completable login() {
        return authentificationUtil.loginCompletable(loginCredentials);
    }

    private Completable loginWithPin() {
        return authentificationUtil.loginCompletable(loginCredentialsWithPin);
    }

    private void assertLoginException(final Class<? extends Exception> exceptionType) throws Exception {
        assertLoginExceptionForLoginType(this::login,
                                         exceptionType,
                                         loginCredentials);
        assertLoginExceptionForLoginType(this::loginWithPin,
                                         exceptionType,
                                         loginCredentialsWithPin);
    }

    private void assertLoginExceptionForLoginType(final Supplier<Completable> loginCall,
                                                  final Class<? extends Exception> exceptionType,
                                                  final LoginCredentials loginCredentials) throws Exception {
        setExceptionOnConnect(loginCredentials, exceptionType);

        final TestObserver<Void> loginSubscriber = loginCall.get().test();

        loginSubscriber.assertError(exceptionType);
    }

    private void assertLoginStateNotification(final LoginState loginState,
                                              final int eventIndex) {
        loginStateSubscriber.assertNoErrors();
        loginStateSubscriber.assertValueCount(eventIndex + 1);

        assertThat(getOnNextEvent(loginStateSubscriber, eventIndex),
                   equalTo(loginState));
    }

    private void setUpLoginError(final Exception error) {
        when(taskExecutorMock.onCurrentThread(any(Action.class)))
            .thenReturn(errorCompletable(error));
    }

    private void setUpLoginDone() {
        when(taskExecutorMock.onCurrentThread(any(Action.class)))
            .thenReturn(emptyCompletable());
    }

    @Test
    public void testLoginStateAfterCreationIsLoggedOut() {
        assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
    }

    @Test
    public void testLogoutAfterCreationDoesNotCallDisconnect() {
        authentificationUtil.logout();

        verify(clientMock, never()).disconnect();
    }

    @Test
    public void testCorrectExceptionForInvalidCredentials() throws Exception {
        setUpLoginError(new JFAuthenticationException(""));

        assertLoginException(JFAuthenticationException.class);
    }

    @Test
    public void testCorrectExceptionForInvalidVersion() throws Exception {
        setUpLoginError(new JFVersionException(""));

        assertLoginException(JFVersionException.class);
    }

    @Test
    public void testCorrectExceptionForException() throws Exception {
        setUpLoginError(new Exception(""));

        assertLoginException(Exception.class);
    }

    @Test
    public void testLoginWithPinCallsClientWithPin() throws Exception {
        setUpLoginDone();

        loginWithPin().subscribe();

        verifyConnectCall(loginCredentialsWithPin, 1);
    }

    public class AfterLogin {

        protected TestObserver<Void> loginCompletionSubscriber;

        @Before
        public void setUp() {
            setUpLoginDone();

            loginCompletionSubscriber = login().test();

            loginCompletionSubscriber.assertNoErrors();
        }

        @Test
        public void testLoginCallsClient() throws Exception {
            verifyConnectCall(loginCredentials, 1);
        }

        @Test
        public void testLoginStateStillLoggedOut() {
            assertThat(authentificationUtil.loginState(), equalTo(LoginState.LOGGED_OUT));
        }

        @Test
        public void testNoLogoutPossibleYet() {
            authentificationUtil.logout();

            verify(clientMock, never()).disconnect();
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
                loginCompletionSubscriber.assertComplete();
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
