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

    public class NoReconnectComposer {

        @Test
        public void noClientReconnect() {
            verifyZeroInteractions(clientMock);
        }

        public class WhenLoggedIn {

            @Before
            public void setUp() {
                loginStateSubject.onNext(LoginState.LOGGED_IN);
            }

            @Test
            public void noClientReconnect() {
                verifyZeroInteractions(clientMock);
            }

            public class WhenConnected {

                @Before
                public void setUp() {
                    connectionStateSubject.onNext(ConnectionState.CONNECTED);
                }

                @Test
                public void noClientReconnect() {
                    verifyZeroInteractions(clientMock);
                }

                public class WhenLoggedOut {

                    @Before
                    public void setUp() {
                        loginStateSubject.onNext(LoginState.LOGGED_OUT);
                    }

                    @Test
                    public void noClientReconnect() {
                        verifyZeroInteractions(clientMock);
                    }

                    public class WhenDisconnected {

                        @Before
                        public void setUp() {
                            connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                        }

                        @Test
                        public void noClientReconnect() {
                            verifyZeroInteractions(clientMock);
                        }
                    }
                }

                public class WhenDisconnected {

                    @Before
                    public void setUp() {
                        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                    }

                    @Test
                    public void noClientReconnect() {
                        verifyZeroInteractions(clientMock);
                    }
                }
            }
        }
    }

    public class WithReconnectComposer {

        @Before
        public void setUp() {
            connectionKeeper.setReconnectComposer(obs -> obs.retry(2));
        }

        public class WhenLoggedIn {

            @Before
            public void setUp() {
                loginStateSubject.onNext(LoginState.LOGGED_IN);
            }

            @Test
            public void noClientReconnect() {
                verifyZeroInteractions(clientMock);
            }

            public class WhenConnected {

                @Before
                public void setUp() {
                    connectionStateSubject.onNext(ConnectionState.CONNECTED);
                }

                @Test
                public void noClientReconnect() {
                    verifyZeroInteractions(clientMock);
                }

                public class WhenLoggedOut {

                    @Before
                    public void setUp() {
                        loginStateSubject.onNext(LoginState.LOGGED_OUT);
                    }

                    @Test
                    public void noClientReconnect() {
                        verifyZeroInteractions(clientMock);
                    }

                    public class WhenDisconnected {

                        @Before
                        public void setUp() {
                            connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                        }

                        @Test
                        public void noClientReconnect() {
                            verifyZeroInteractions(clientMock);
                        }
                    }
                }

                public class WhenDisconnected {

                    @Before
                    public void setUp() {
                        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                    }

                    @Test
                    public void clientCallsReconnect() {
                        verify(clientMock).reconnect();
                    }

                    public class FirstReconnectIsSuccessful {

                        @Before
                        public void setUp() {
                            connectionStateSubject.onNext(ConnectionState.CONNECTED);
                        }

                        public class WhenAgainDisconnected {

                            @Before
                            public void setUp() {
                                connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                            }

                            @Test
                            public void clientCallsReconnect() {
                                verify(clientMock, times(2)).reconnect();
                            }
                        }
                    }

                    public class SecondReconnectFail {

                        @Before
                        public void setUp() {
                            connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                        }

                        @Test
                        public void clientCallsReconnect() {
                            verify(clientMock, times(2)).reconnect();
                        }

                        public class ThirdReconnectFail {

                            @Before
                            public void setUp() {
                                connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                            }

                            @Test
                            public void clientCallsReconnect() {
                                verify(clientMock, times(3)).reconnect();
                            }

                            public class f4ReconnectFail {

                                @Before
                                public void setUp() {
                                    connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
                                }

                                @Test
                                public void clientCallsReconnect() {
                                    verify(clientMock, times(3)).reconnect();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
