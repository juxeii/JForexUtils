package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.client.JFSystemListener;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;

import rx.observers.TestSubscriber;

public class JFSystemListenerTest {

    private JFSystemListener jfSystemListener;

    private final TestSubscriber<StrategyRunData> strategyInfoSubscriber = new TestSubscriber<>();
    private final TestSubscriber<ConnectionState> connectionStateSubscriber = new TestSubscriber<>();
    private final long processID = 42L;

    @Before
    public void setUp() {
        jfSystemListener = new JFSystemListener();

        jfSystemListener.strategyRunDataObservable().subscribe(strategyInfoSubscriber);
        jfSystemListener.connectionStateObservable().subscribe(connectionStateSubscriber);
    }

    private void assertStrategyInfoNotification(final StrategyRunState strategyState) {
        strategyInfoSubscriber.assertNoErrors();
        strategyInfoSubscriber.assertValueCount(1);

        final StrategyRunData strategyInfo = strategyInfoSubscriber.getOnNextEvents().get(0);

        assertThat(strategyInfo.processID(), equalTo(processID));
        assertThat(strategyInfo.state(), equalTo(strategyState));
    }

    private void assertConnectionStateNotification(final ConnectionState expectedconnectionState) {
        connectionStateSubscriber.assertNoErrors();
        connectionStateSubscriber.assertValueCount(1);

        final ConnectionState connectionState = connectionStateSubscriber.getOnNextEvents().get(0);

        assertThat(connectionState, equalTo(expectedconnectionState));
    }

    @Test
    public void testOnStrategyStartNotifiesSubscriber() {
        jfSystemListener.onStart(processID);

        assertStrategyInfoNotification(StrategyRunState.STARTED);
    }

    @Test
    public void testOnStrategyStopNotifiesSubscriber() {
        jfSystemListener.onStop(processID);

        assertStrategyInfoNotification(StrategyRunState.STOPPED);
    }

    @Test
    public void testStrategyInfoUnsubscribesIsCorrect() {
        strategyInfoSubscriber.unsubscribe();

        jfSystemListener.onStart(processID);

        strategyInfoSubscriber.assertNoErrors();
        strategyInfoSubscriber.assertValueCount(0);
    }

    @Test
    public void testOnConnectMessageNotifiesSubscriber() {
        jfSystemListener.onConnect();

        assertConnectionStateNotification(ConnectionState.CONNECTED);
    }

    @Test
    public void testOnDisonnectMessageNotifiesSubscriber() {
        jfSystemListener.onDisconnect();

        assertConnectionStateNotification(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testConnectionStateUnsubscribesIsCorrect() {
        connectionStateSubscriber.unsubscribe();

        jfSystemListener.onConnect();

        connectionStateSubscriber.assertNoErrors();
        connectionStateSubscriber.assertValueCount(0);
    }
}
