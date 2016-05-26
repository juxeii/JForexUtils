package com.jforex.programming.connection.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionKeeper;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.common.CommonUtilForTest;

import com.dukascopy.api.system.IClient;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class ConnectionKeeperTest extends CommonUtilForTest {

    @Mock
    private IClient clientMock;
    @Mock
    private AuthentificationUtil authentificationUtilMock;
    private final Subject<ConnectionState, ConnectionState> connectionStateObs = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();

        new ConnectionKeeper(clientMock,
                             connectionStateObs,
                             authentificationUtilMock);
    }

    public class IsInLoginState {

        @Before
        public void setUp() {
            when(authentificationUtilMock.loginState()).thenReturn(LoginState.LOGGED_IN);
        }

        @Test
        public void testConnectedMessageIsIgnored() {
            connectionStateObs.onNext(ConnectionState.CONNECTED);

            verifyZeroInteractions(authentificationUtilMock);
        }

        @Test
        public void testLightReconnectAreDone() {
            connectionStateObs.onNext(ConnectionState.DISCONNECTED);

            connectionStateObs.onNext(ConnectionState.DISCONNECTED);

            connectionStateObs.onNext(ConnectionState.DISCONNECTED);

            verify(clientMock, times(3)).reconnect();
        }
    }
}
