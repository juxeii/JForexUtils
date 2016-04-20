package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.connection.ConnectionMonitor;
import com.jforex.programming.connection.ConnectionState;

import rx.observers.TestSubscriber;

public class ConnectionMonitorTest {

    private ConnectionMonitor connectionMonitor;

    private final TestSubscriber<ConnectionState> subscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        connectionMonitor = new ConnectionMonitor();
        connectionMonitor.connectionObs().subscribe(subscriber);
    }

    private void assertState(final ConnectionState connectionState) {
        assertThat(connectionMonitor.state(), equalTo(connectionState));
    }

    private void assertChangeEvent(final ConnectionState connectionState) {
        assertState(connectionState);
        assertSubscriberIsNotified(connectionState);
    }

    private void assertSubscriberIsNotified(final ConnectionState expectedState) {
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final ConnectionState receivedState = subscriber.getOnNextEvents().get(0);

        assertThat(receivedState, equalTo(expectedState));
    }

    @Test
    public void testAfterCreationStateIsLoggedOut() {
        assertState(ConnectionState.LOGGED_OUT);
    }

    @Test
    public void testOnLoginChangesStateCorrect() {
        connectionMonitor.onLogin();

        assertChangeEvent(ConnectionState.LOGGED_IN);
    }

    @Test
    public void testOnLogoutChangesStateCorrect() {
        connectionMonitor.onLogout();

        assertChangeEvent(ConnectionState.LOGGED_OUT);
    }

    @Test
    public void testOnDisconnectChangesStateCorrect() {
        connectionMonitor.onDisconnect();

        assertChangeEvent(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testAfterUnsubscribeNoMoreNotifies() {
        subscriber.unsubscribe();

        connectionMonitor.onLogin();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }
}
