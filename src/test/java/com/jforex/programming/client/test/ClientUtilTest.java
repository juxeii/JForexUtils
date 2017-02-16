package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.observers.TestObserver;

public class ClientUtilTest extends CommonUtilForTest {

    private ClientUtil clientUtil;

    private TestObserver<ConnectionState> connectionStateSubscriber;
    private TestObserver<StrategyRunData> runDataSubscriber;
    private final String cacheDirectory = "cacheDirectory";
    private final long processID = 42L;

    @Before
    public void setUp() throws Exception {
        clientUtil = new ClientUtil(clientMock, cacheDirectory);

        connectionStateSubscriber = clientUtil
            .observeConnectionState()
            .test();
        runDataSubscriber = clientUtil
            .observeStrategyRunData()
            .test();
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
    public void clientReturnsCorrectInstance() {
        assertThat(clientUtil.client(), equalTo(clientMock));
    }

    @Test
    public void cacheDirectoryIsInitialized() {
        verify(clientMock)
            .setCacheDirectory(argThat(file -> file.getName().equals(cacheDirectory)));
    }

    @Test
    public void authentificationIsValid() {
        assertNotNull(clientUtil.authentification());
    }

    @Test
    public void reconnectorIsValid() {
        assertNotNull(clientUtil.reconnector());
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
