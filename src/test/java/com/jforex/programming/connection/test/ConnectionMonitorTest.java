package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.connection.ConnectionMonitor;
import com.jforex.programming.connection.ConnectionState;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ConnectionMonitorTest {

    private ConnectionMonitor connectionMonitor;

    private final Subject<ConnectionState, ConnectionState> connectionStateSubject = PublishSubject.create();
    private final TestSubscriber<ConnectionState> subscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        connectionMonitor = new ConnectionMonitor(connectionStateSubject);
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
    public void testAfterCreationStateIsDisconnected() {
        assertState(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testOnConnectChangesStateCorrect() {
        connectionStateSubject.onNext(ConnectionState.CONNECTED);

        assertChangeEvent(ConnectionState.CONNECTED);
    }

    @Test
    public void testOnDisconnectChangesStateCorrect() {
        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

        assertChangeEvent(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testAfterUnsubscribeNoMoreNotifies() {
        subscriber.unsubscribe();

        connectionStateSubject.onNext(ConnectionState.CONNECTED);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }
}
