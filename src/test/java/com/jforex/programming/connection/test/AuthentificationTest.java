package com.jforex.programming.connection.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class AuthentificationTest extends CommonUtilForTest {

    private Authentification authentification;

    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private TestObserver<LoginState> loginStateSubscriber;

    @Before
    public void setUp() {
        authentification = new Authentification(clientMock, loginStatePublisher);
    }

    private void login(final LoginCredentials credentials) {
        authentification
            .login(credentials)
            .subscribe(() -> {},
                       t -> {});
    }

    @Test
    public void logoutDoesNotCallsDisconnectOnClientWhenNotSubscribed() {
        authentification.logout();

        verifyZeroInteractions(clientMock);
    }

    @Test
    public void loginWithPinCallsConnectClientWithPin() throws Exception {
        login(loginCredentialsWithPin);

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

        login(loginCredentialsWithPin);

        loginStateSubscriber.assertNoValues();
    }

    public class WhenLoggedIn {

        @Before
        public void setUp() {
            loginStateSubscriber = loginStatePublisher
                .observable()
                .test();

            login(loginCredentials);
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
