package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFSystemListener;
import com.jforex.programming.misc.StrategyInfo;
import com.jforex.programming.misc.StrategyState;

import rx.observers.TestSubscriber;

public class JFSystemListenerTest {

    private JFSystemListener jfSystemListener;

    private final TestSubscriber<ConnectionState> connectionStateSubscriber = new TestSubscriber<>();
    private final TestSubscriber<StrategyInfo> strategyInfoSubscriber = new TestSubscriber<>();
    private final long strategyProcessID = 42L;

    @Before
    public void setUp() {
        jfSystemListener = new JFSystemListener();
        jfSystemListener.connectionObs().subscribe(connectionStateSubscriber);
        jfSystemListener.strategyObs().subscribe(strategyInfoSubscriber);
    }

    private void assertConnectionSubscriberIsNotified(final ConnectionState expectedState) {
        connectionStateSubscriber.assertNoErrors();
        connectionStateSubscriber.assertValueCount(1);
        final ConnectionState receivedState = connectionStateSubscriber.getOnNextEvents().get(0);

        assertThat(receivedState, equalTo(expectedState));
    }

    private void assertStrategyInfoSubscriberIsNotified(final StrategyInfo strategyInfo) {
        strategyInfoSubscriber.assertNoErrors();
        strategyInfoSubscriber.assertValueCount(1);
        final StrategyInfo receivedStrategyInfo = strategyInfoSubscriber.getOnNextEvents().get(0);

        assertThat(receivedStrategyInfo.processID(), equalTo(strategyInfo.processID()));
        assertThat(receivedStrategyInfo.state(), equalTo(strategyInfo.state()));
    }

    private <T> void testAfterUnsubscribeNoMoreNotifies(final TestSubscriber<T> testSubscriber,
                                                        final Runnable systemListenerCall) {
        testSubscriber.unsubscribe();

        systemListenerCall.run();

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(0);
    }

    @Test
    public void testConnectionSubscriberIsNotifiedOnConnect() {
        jfSystemListener.onConnect();

        assertConnectionSubscriberIsNotified(ConnectionState.CONNECTED);
    }

    @Test
    public void testConnectionSubscriberIsNotifiedOnDisconnect() {
        jfSystemListener.onDisconnect();

        assertConnectionSubscriberIsNotified(ConnectionState.DISCONNECTED);
    }

    @Test
    public void testAfterUnsubscribeNoMoreConnectionNotifies() {
        testAfterUnsubscribeNoMoreNotifies(connectionStateSubscriber,
                                           () -> jfSystemListener.onConnect());
    }

    @Test
    public void testStrategyInfoSubscriberIsNotifiedOnStart() {
        jfSystemListener.onStart(strategyProcessID);

        assertStrategyInfoSubscriberIsNotified(new StrategyInfo(strategyProcessID, StrategyState.STARTED));
    }

    @Test
    public void testStrategyInfoSubscriberIsNotifiedOnStop() {
        jfSystemListener.onStop(strategyProcessID);

        assertStrategyInfoSubscriberIsNotified(new StrategyInfo(strategyProcessID, StrategyState.STOPPED));
    }

    @Test
    public void testAfterUnsubscribeNoMoreStrategyInfoNotifies() {
        testAfterUnsubscribeNoMoreNotifies(strategyInfoSubscriber,
                                           () -> jfSystemListener.onStart(strategyProcessID));
    }
}
