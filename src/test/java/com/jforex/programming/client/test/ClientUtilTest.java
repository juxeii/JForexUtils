package com.jforex.programming.client.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observers.TestObserver;

public class ClientUtilTest extends CommonUtilForTest {

    private ClientUtil clientUtil;

    private TestObserver<ConnectionState> connectionStateSubscriber;
    private TestObserver<StrategyRunData> runDataSubscriber;
    private final String cacheDirectory = "cacheDirectory";
    private final long processID = 42L;
    private final ObservableTransformer<ConnectionState, ConnectionState> reconnectComposer = upstream -> upstream;

    @Before
    public void setUp() throws Exception {
        clientUtil = new ClientUtil(clientMock, cacheDirectory);

        connectionStateSubscriber = clientUtil
            .observeConnectionState()
            .test();
        runDataSubscriber = clientUtil
            .observeStrategyRunData()
            .test();
        clientUtil.setReconnectComposer(reconnectComposer);
        reconnectComposer.apply(Observable.empty());
    }

    private void assertConnectionState(final ConnectionState connectionState) {
        connectionStateSubscriber
            .assertNoErrors()
            .assertValue(connectionState);
    }

    private void assertStrategyRunState(final StrategyRunState strategyRunState) {
        runDataSubscriber
            .assertNoErrors()
            .assertValue(new StrategyRunData(processID, strategyRunState));
    }

    @Test
    public void cacheDirectoryIsInitialized() {
        verify(clientMock)
            .setCacheDirectory(argThat(file -> file.getName().equals(cacheDirectory)));
    }

    @Test
    public void loginCompletableIsValid() {
        assertNotNull(clientUtil.login(loginCredentials));
    }

    @Test
    public void logoutCompletableIsValid() {
        assertNotNull(clientUtil.logout());
    }

    @Test
    public void connectMessageIsPublished() {
        clientForTest.publishConnected();

        assertConnectionState(ConnectionState.CONNECTED);
    }

    @Test
    public void disconnectMessageIsPublished() {
        clientForTest.publishDisconnected();

        assertConnectionState(ConnectionState.DISCONNECTED);
    }

    @Test
    public void strategyStartIsPublished() {
        clientForTest.publishStrategyStarted(processID);

        assertStrategyRunState(StrategyRunState.STARTED);
    }

    @Test
    public void strategyStopIsPublished() {
        clientForTest.publishStrategyStopped(processID);

        assertStrategyRunState(StrategyRunState.STOPPED);
    }

    @Test
    public void pinCaptchaIsValid() {
        assertNotNull(clientUtil.pinCaptcha());
    }
}
