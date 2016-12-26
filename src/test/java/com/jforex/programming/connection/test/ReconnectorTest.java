package com.jforex.programming.connection.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.Reconnector;
import com.jforex.programming.connection.UserConnection;
import com.jforex.programming.connection.UserConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class ReconnectorTest extends CommonUtilForTest {

    private Reconnector reconnector;

    @Mock
    private Authentification authentificationMock;
    @Mock
    private UserConnection userConnectionMock;
    @Mock
    private Action actionMock;
    private final Subject<UserConnectionState> userConnectionState = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        reconnector = new Reconnector(clientMock,
                                      authentificationMock,
                                      userConnectionMock);
    }

    public void setUpMocks() {
        when(userConnectionMock.observe()).thenReturn(userConnectionState);

        when(authentificationMock.login(loginCredentials)).thenReturn(Completable.complete());
    }

    private void sendConnect() {
        userConnectionState.onNext(UserConnectionState.CONNECTED);
    }

    private void sendDisconnect() {
        userConnectionState.onNext(UserConnectionState.DISCONNECTED);
    }

    public class LightReconnect {

        private TestObserver<Void> strategy;

        @Before
        public void setUp() {
            strategy = reconnector
                .lightReconnect()
                .test();
        }

        @Test
        public void clientReconnectIsCalled() {
            verify(clientMock).reconnect();
        }

        @Test
        public void strategyNotCompletedOnDisconnect() {
            sendDisconnect();

            strategy.assertNotComplete();
        }

        @Test
        public void strategyNotCompletedOnLogout() {
            userConnectionState.onNext(UserConnectionState.LOGGED_OUT);

            strategy.assertNotComplete();
        }

        @Test
        public void strategyCompletedOnConnectedValue() {
            sendConnect();

            strategy.assertComplete();
        }
    }

    public class Relogin {

        private TestObserver<Void> strategy;

        @Before
        public void setUp() {
            strategy = reconnector
                .relogin(loginCredentials)
                .test();
        }

        @Test
        public void loginOnAuthentificationIsCalled() {
            verify(authentificationMock).login(loginCredentials);
        }

        @Test
        public void strategyNotCompletedOnValueOtherThanConnected() {
            sendDisconnect();

            strategy.assertNotComplete();
        }

        @Test
        public void strategyCompletedOnConnectedValue() {
            sendConnect();

            strategy.assertComplete();
        }
    }

    public class ApplyStrategy {

        @Before
        public void setUp() {
            reconnector.applyStrategy(reconnector
                .lightReconnect()
                .retry(2));
        }

        @Test
        public void noActionCallWhenUserConnectionConnects() {
            sendConnect();

            verifyZeroInteractions(actionMock);
        }

        @Test
        public void noActionCallWhenUserConnectionLogsout() {
            userConnectionState.onNext(UserConnectionState.LOGGED_OUT);

            verifyZeroInteractions(actionMock);
        }

        public class OnUserConnectionDisconnect {

            @Before
            public void setUp() {
                sendDisconnect();
            }

            @Test
            public void actionIsExecuted() throws Exception {
                verify(clientMock).reconnect();
            }

            @Test
            public void userConnectionIsMonitoredAgainOnSuccess() {
                sendConnect();
                sendDisconnect();
                sendConnect();

                verify(clientMock, times(2)).reconnect();
            }

            @Test
            public void userConnectionIsMonitoredAgainOnError() {
                sendDisconnect();
                sendDisconnect();
                sendDisconnect();

                verify(clientMock, times(3)).reconnect();

                sendDisconnect();
                sendDisconnect();
                sendDisconnect();

                verify(clientMock, times(6)).reconnect();
            }
        }
    }
}
