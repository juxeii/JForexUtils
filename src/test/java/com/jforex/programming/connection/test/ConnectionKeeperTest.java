package com.jforex.programming.connection.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.connection.ConnectionKeeper;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class ConnectionKeeperTest extends CommonUtilForTest {

    private ConnectionKeeper connectionKeeper;

    private final Subject<ConnectionState> connectionStateSubject = PublishSubject.create();
    private final Subject<LoginState> loginStateSubject = PublishSubject.create();

    @Before
    public void setUp() {
        connectionKeeper = new ConnectionKeeper(clientMock,
                                                connectionStateSubject,
                                                loginStateSubject);
    }

    @Test
    public void whenNotStartedNoReconnectOnDisconnectAndLoggedIn() {
        loginStateSubject.onNext(LoginState.LOGGED_IN);
        connectionStateSubject.onNext(ConnectionState.CONNECTED);

        verifyZeroInteractions(clientMock);
    }

    @Test
    public void callStopWithoutStartedDoesNothing() {
        connectionKeeper.stop();
    }

    public class WhenStarted {

        @Before
        public void setUp() {
            connectionKeeper.start();
        }

        @Test
        public void secondStartCallDoesNothing() {
            connectionKeeper.start();
        }

        @Test
        public void whenLoggedOutNoClientInteraction() {
            verifyZeroInteractions(clientMock);
        }

        public class WhenLoggedIn {

            @Before
            public void setUp() {
                loginStateSubject.onNext(LoginState.LOGGED_IN);
            }

            @Test
            public void whenDisconnectedReconnectIsCalledOnClient() {
                connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

                verify(clientMock).reconnect();
            }

            public class WhenConnected {

                @Before
                public void setUp() {
                    connectionStateSubject.onNext(ConnectionState.CONNECTED);
                }

                @Test
                public void noClientInteraction() {
                    verifyZeroInteractions(clientMock);
                }

                @Test
                public void whenStoppedNoReconnectOnClientOnDisconnect() {
                    connectionKeeper.stop();

                    connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

                    verifyZeroInteractions(clientMock);
                }

                @Test
                public void whenDisconnectedReconnectIsCalledOnClient() {
                    connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

                    verify(clientMock).reconnect();
                }

                @Test
                public void whenLoggedOutOnDisconnectNoReconnectCall() {
                    loginStateSubject.onNext(LoginState.LOGGED_OUT);

                    connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

                    verifyZeroInteractions(clientMock);
                }

                @Test
                public void multipleStopCallsDoNothing() {
                    connectionKeeper.stop();
                    connectionKeeper.stop();
                }
            }
        }
    }
}
