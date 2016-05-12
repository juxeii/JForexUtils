package com.jforex.programming.connection.test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionKeeper;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class ConnectionKeeperTest extends CommonUtilForTest {

    @Mock private AuthentificationUtil authentificationUtilMock;
    private final Subject<ConnectionState, ConnectionState> connectionStateObs = PublishSubject.create();
    private final static String jnlpAddress = "http://jnlp.test.address";
    private final static String userName = "username";
    private final static String password = "password";
    private LoginCredentials loginCredentials;
    private int noOfLightReconnects;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    @Before
    public void setUp() {
        initCommonTestFramework();
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                userName,
                                                password);
        noOfLightReconnects = platformSettings.noOfLightReconnects();
        new ConnectionKeeper(connectionStateObs,
                             authentificationUtilMock,
                             loginCredentials);
    }

    public class IsInLoginState {

        @Before
        public void setUp() {
            when(authentificationUtilMock.state()).thenReturn(LoginState.LOGGED_IN);
        }

        @Test
        public void testConnectedMessageIsIgnored() {
            connectionStateObs.onNext(ConnectionState.CONNECTED);

            verifyZeroInteractions(authentificationUtilMock);
        }

        public class AfterDisconnectMessage {

            @Before
            public void setUp() {
                connectionStateObs.onNext(ConnectionState.DISCONNECTED);
            }

            @Test
            public void testLightReconnectIsTriggered() {
                verify(authentificationUtilMock).reconnect();
            }

            public class AfterConnectedMessage {

                @Before
                public void setUp() {
                    connectionStateObs.onNext(ConnectionState.CONNECTED);
                }

                @Test
                public void testNoMoreLightReconnectsOrLogins() {
                    verify(authentificationUtilMock).reconnect();
                    verify(authentificationUtilMock, never()).login(loginCredentials);
                }

                public class AfterAllLightReconnects {

                    @Before
                    public void setUp() {
                        for (int i = 0; i < noOfLightReconnects; ++i)
                            connectionStateObs.onNext(ConnectionState.DISCONNECTED);
                    }

                    @Test
                    public void testLightReconnectIsTriggeredTwice() {
                        verify(authentificationUtilMock, times(noOfLightReconnects + 1)).reconnect();
                    }
                }
            }

            public class TriggerAllLightReconnects {

                @Before
                public void setUp() {
                    for (int i = 0; i < noOfLightReconnects; ++i)
                        connectionStateObs.onNext(ConnectionState.DISCONNECTED);
                }

                @Test
                public void testLightReconnectIsTriggeredTwice() {
                    verify(authentificationUtilMock, times(noOfLightReconnects)).reconnect();
                }

                public class AfterAllLightReconnects {

                    @Test
                    public void testLoginIsCalled() {
                        verify(authentificationUtilMock).login(loginCredentials);
                    }
                }
            }
        }
    }
}
