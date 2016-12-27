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
    private Action loginActionMock;
    private final Subject<UserConnectionState> userConnectionSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        reconnector = new Reconnector(clientMock,
                                      authentificationMock,
                                      userConnectionMock);
    }

    public void setUpMocks() {
        when(userConnectionMock.observe()).thenReturn(userConnectionSubject);

        when(authentificationMock.login(loginCredentials)).thenReturn(Completable.fromAction(loginActionMock));
    }

    private void sendConnect() {
        sendUserConnectionState(UserConnectionState.CONNECTED);
    }

    private void sendDisconnect() {
        sendUserConnectionState(UserConnectionState.DISCONNECTED);
    }

    private void sendLogout() {
        sendUserConnectionState(UserConnectionState.LOGGED_OUT);
    }

    private void sendUserConnectionState(final UserConnectionState userConnectionState) {
        userConnectionSubject.onNext(userConnectionState);
    }

    private void verifyNoMockCalls() {
        verifyZeroInteractions(clientMock);
        verifyZeroInteractions(loginActionMock);
    }

    public class NoCompose {

        @Test
        public void noMockCallsOnConnect() {
            sendConnect();

            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnDisconnect() {
            sendDisconnect();

            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnLogout() {
            sendLogout();

            verifyNoMockCalls();
        }
    }

    public class ComposeLightReconnect {

        @Before
        public void setUp() {
            reconnector.composeLightReconnect(x -> x);
        }

        @Test
        public void noMockCalls() {
            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnConnect() {
            sendConnect();

            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnLogout() {
            sendLogout();

            verifyNoMockCalls();
        }

        public class OnDisconnectLightStrategyStart {

            @Before
            public void setUp() {
                sendDisconnect();
            }

            @Test
            public void reconnectOnClientIsCalled() {
                verify(clientMock).reconnect();
            }

            @Test
            public void noLoginCall() {
                verifyZeroInteractions(loginActionMock);
            }

            @Test
            public void logoutIsIgnored() {
                sendLogout();
                sendDisconnect();

                verify(clientMock).reconnect();
            }

            @Test
            public void nextDisconnectRestartsLightStrategy() {
                sendDisconnect();
                sendDisconnect();

                verify(clientMock, times(2)).reconnect();
            }
        }

        public class ComposeRelogin {

            @Before
            public void setUp() {
                reconnector.composeLoginReconnect(loginCredentials, x -> x);
            }

            @Test
            public void noMockCalls() {
                verifyNoMockCalls();
            }

            @Test
            public void noMockCallsOnConnect() {
                sendConnect();

                verifyNoMockCalls();
            }

            @Test
            public void noMockCallsOnLogout() {
                sendLogout();

                verifyNoMockCalls();
            }

            public class OnDisconnect {

                @Before
                public void setUp() {
                    sendDisconnect();
                }

                @Test
                public void firstReconnectCall() {
                    verify(clientMock).reconnect();
                }

                public class OnConnect {

                    @Before
                    public void setUp() {
                        sendConnect();
                    }

                    @Test
                    public void lightReconnectStrategyRestarted() {
                        sendDisconnect();
                        verify(clientMock, times(2)).reconnect();
                    }
                }

                public class OnSecondDisconnect {

                    @Before
                    public void setUp() {
                        sendDisconnect();
                    }

                    @Test
                    public void reloginStrategyIsStarted() throws Exception {
                        verify(loginActionMock).run();
                    }

                    public class OnThirdDisconnect {

                        @Before
                        public void setUp() {
                            sendDisconnect();
                        }

                        @Test
                        public void lightReconnectStrategyRestarted() {
                            sendDisconnect();
                            verify(clientMock, times(2)).reconnect();
                        }
                    }
                }
            }
        }
    }

    public class ComposeRelogin {

        @Before
        public void setUp() {
            reconnector.composeLoginReconnect(loginCredentials, x -> x);
        }

        @Test
        public void noMockCalls() {
            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnConnect() {
            sendConnect();

            verifyNoMockCalls();
        }

        @Test
        public void noMockCallsOnLogout() {
            sendLogout();

            verifyNoMockCalls();
        }

        public class OnDisconnect {

            @Before
            public void setUp() {
                sendDisconnect();
            }

            @Test
            public void noReconnectCall() {
                verifyZeroInteractions(clientMock);
            }

            @Test
            public void loginOnAuthentificationIsCalled() throws Exception {
                verify(loginActionMock).run();
            }

            @Test
            public void reloginFailureRestartsLoginStrategy() throws Exception {
                sendDisconnect();
                sendDisconnect();

                verify(loginActionMock, times(2)).run();
            }

            @Test
            public void reloginSuccessRestartsLoginStrategy() throws Exception {
                sendConnect();
                sendDisconnect();

                verify(loginActionMock, times(2)).run();
            }
        }
    }
}
