package com.jforex.programming.client.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.client.JFSystemListener;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class JFSystemListenerTest extends CommonUtilForTest {

    private JFSystemListener jfSystemListener;

    private TestObserver<StrategyRunData> runDataSubscriber;
    private TestObserver<ConnectionState> connectionStateSubscriber;

    @Before
    public void setUp() {
        jfSystemListener = new JFSystemListener();

        runDataSubscriber = jfSystemListener
            .observeStrategyRunData()
            .test();
        connectionStateSubscriber = jfSystemListener
            .observeConnectionState()
            .test();
    }

    public class WhenOnStart {

        private final long processID = 42;

        private StrategyRunData createRunData(final StrategyRunState state) {
            return new StrategyRunData(processID, state);
        }

        @Before
        public void setUp() {
            jfSystemListener.onStart(processID);
        }

        @Test
        public void onStartRunDataIsPublishedCorrect() {
            runDataSubscriber.assertValue(createRunData(StrategyRunState.STARTED));
        }

        @Test
        public void onStopRunDataIsPublishedCorrect() {
            jfSystemListener.onStop(processID);

            runDataSubscriber.assertValues(createRunData(StrategyRunState.STARTED),
                                           createRunData(StrategyRunState.STOPPED));
        }
    }

    public class WhenOnConnect {

        @Before
        public void setUp() {
            jfSystemListener.onConnect();
        }

        @Test
        public void onConnectIsPublishedCorrect() {
            connectionStateSubscriber.assertValue(ConnectionState.CONNECTED);
        }

        @Test
        public void onDisconnectIsPublishedCorrect() {
            jfSystemListener.onDisconnect();

            connectionStateSubscriber.assertValues(ConnectionState.CONNECTED,
                                                   ConnectionState.DISCONNECTED);
        }
    }
}
