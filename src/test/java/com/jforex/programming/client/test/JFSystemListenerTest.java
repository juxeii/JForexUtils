package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.client.JFSystemListener;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class JFSystemListenerTest extends CommonUtilForTest {

    private JFSystemListener jfSystemListener;

    private final TestSubscriber<StrategyRunData> runDataSubscriber = new TestSubscriber<>();
    private final TestSubscriber<ConnectionState> connectionStateSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        jfSystemListener = new JFSystemListener();

        jfSystemListener
            .strategyRunDataFlowable()
            .subscribe(runDataSubscriber);
        jfSystemListener
            .connectionStateFlowable()
            .subscribe(connectionStateSubscriber);
    }

    private void assertSubscriberCount(final TestSubscriber<?> subscriber,
                                       final int itemIndex) {
        subscriber.assertNoErrors();
        subscriber.assertNotComplete();
        subscriber.assertValueCount(itemIndex + 1);
    }

    public class WhenOnStart {

        private final long processID = 42;

        private void assertRunData(final StrategyRunState runState,
                                   final int itemIndex) {
            assertSubscriberCount(runDataSubscriber, itemIndex);

            final StrategyRunData runData = getOnNextEvent(runDataSubscriber, itemIndex);
            assertThat(runData.processID(), equalTo(processID));
            assertThat(runData.state(), equalTo(runState));
        }

        @Before
        public void setUp() {
            jfSystemListener.onStart(processID);
        }

        @Test
        public void onStartRunDataIsPublishedCorrect() {
            assertRunData(StrategyRunState.STARTED, 0);
        }

        @Test
        public void onStopRunDataIsPublishedCorrect() {
            jfSystemListener.onStop(processID);

            assertRunData(StrategyRunState.STOPPED, 1);
        }
    }

    public class WhenOnConnect {

        private void assertConnectionState(final ConnectionState connectionState,
                                           final int itemIndex) {
            assertSubscriberCount(connectionStateSubscriber, itemIndex);

            assertThat(getOnNextEvent(connectionStateSubscriber, itemIndex),
                       equalTo(connectionState));
        }

        @Before
        public void setUp() {
            jfSystemListener.onConnect();
        }

        @Test
        public void onConnectIsPublishedCorrect() {
            assertConnectionState(ConnectionState.CONNECTED, 0);
        }

        @Test
        public void onDisconnectIsPublishedCorrect() {
            jfSystemListener.onDisconnect();

            assertConnectionState(ConnectionState.DISCONNECTED, 1);
        }
    }
}
