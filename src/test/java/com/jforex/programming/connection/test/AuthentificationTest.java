package com.jforex.programming.connection.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionLostException;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationTest extends CommonUtilForTest {

    private Authentification authentification;

    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private final Subject<ConnectionState> connectionStateSubject = PublishSubject.create();
    private TestObserver<LoginState> loginStateSubscriber;

    @Before
    public void setUp() {
        authentification = new Authentification(clientMock,
                                                connectionStateSubject,
                                                loginStatePublisher);
    }

    private void loginWithConnectState(final LoginCredentials credentials) {
        authentification
            .login(credentials)
            .subscribe(() -> {},
                       t -> {});
        connectionStateSubject.onNext(ConnectionState.CONNECTED);
    }

    @Test
    public void connectionLostExceptionWhenStateIsNotConnected() {
        loginStateSubscriber = loginStatePublisher
            .observable()
            .test();

        final TestObserver<Void> testObserver = authentification
            .login(loginCredentials)
            .test();
        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

        testObserver.assertError(ConnectionLostException.class);
        loginStateSubscriber.assertNoValues();
    }

    @Test
    public void logoutDoesNotCallsDisconnectOnClientWhenNotSubscribed() {
        authentification.logout();

        verifyZeroInteractions(clientMock);
    }

    @Test
    public void loginWithPinCallsConnectClientWithPin() throws Exception {
        loginWithConnectState(loginCredentialsWithPin);

        verify(clientMock).connect(loginCredentialsWithPin.jnlpAddress(),
                                   loginCredentialsWithPin.username(),
                                   loginCredentialsWithPin.password(),
                                   loginCredentialsWithPin.maybePin().get());
    }

    @Test
    public void loginWithExecpetionDoesNotPublishLoginStatss() throws Exception {
        doThrow(jfException)
            .when(clientMock)
            .connect(loginCredentialsWithPin.jnlpAddress(),
                     loginCredentialsWithPin.username(),
                     loginCredentialsWithPin.password(),
                     loginCredentialsWithPin.maybePin().get());
        loginStateSubscriber = loginStatePublisher
            .observable()
            .test();

        loginWithConnectState(loginCredentialsWithPin);

        loginStateSubscriber.assertNoValues();
    }

    public class WhenLoggedIn {

        @Before
        public void setUp() {
            loginStateSubscriber = loginStatePublisher
                .observable()
                .test();

            loginWithConnectState(loginCredentials);
        }

        @Test
        public void connectClientIsCalledWithNoPin() throws Exception {
            verify(clientMock).connect(loginCredentials.jnlpAddress(),
                                       loginCredentials.username(),
                                       loginCredentials.password());
        }

        @Test
        public void loggedInStateIsPublished() {
            loginStateSubscriber.assertValue(LoginState.LOGGED_IN);
        }

        public class WhenLoggedOut {

            @Before
            public void setUp() {
                authentification
                    .logout()
                    .subscribe();
            }

            @Test
            public void disconnectOnClientIsCalled() {
                verify(clientMock).disconnect();
            }

            @Test
            public void loginStateChangedToLoggedOut() {
                loginStateSubscriber.assertValues(LoginState.LOGGED_IN, LoginState.LOGGED_OUT);
            }
        }
    }
}
