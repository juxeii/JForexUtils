package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.connection.ConnectionMonitor;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.test.fakes.IMessageForTest;

import com.dukascopy.api.IMessage;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ConnectionMonitorTest {

    private ConnectionMonitor connectionMonitor;

    private final Subject<LoginState, LoginState> loginStateSubject = PublishSubject.create();
    private final Subject<IMessage, IMessage> messageSubject = PublishSubject.create();
    private final TestSubscriber<ConnectionState> subscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        connectionMonitor = new ConnectionMonitor(loginStateSubject, messageSubject);
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
        loginStateSubject.onNext(LoginState.LOGGED_IN);

        assertChangeEvent(ConnectionState.LOGGED_IN);
    }

    @Test
    public void testOnLogoutChangesStateCorrect() {
        loginStateSubject.onNext(LoginState.LOGGED_OUT);

        assertChangeEvent(ConnectionState.LOGGED_OUT);
    }

    @Test
    public void testOnConnectChangesStateCorrect() {
        final IMessageForTest connectMessage =
                new IMessageForTest(IMessage.Type.CONNECTION_STATUS, "connect");
        messageSubject.onNext(connectMessage);

        assertChangeEvent(ConnectionState.LOGGED_IN);
    }

    @Test
    public void testOnDisconnectChangesStateCorrect() {
        final IMessageForTest disconnectMessage =
                new IMessageForTest(IMessage.Type.CONNECTION_STATUS, "disconnect");
        messageSubject.onNext(disconnectMessage);

        assertChangeEvent(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testAfterUnsubscribeNoMoreNotifies() {
        subscriber.unsubscribe();

        loginStateSubject.onNext(LoginState.LOGGED_IN);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }
}
