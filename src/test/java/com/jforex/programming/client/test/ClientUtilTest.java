package com.jforex.programming.client.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.client.JFSystemListener;
import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observers.TestObserver;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

@RunWith(HierarchicalContextRunner.class)
public class ClientUtilTest extends CommonUtilForTest {

    private ClientUtil clientUtil;

    private TestObserver<ConnectionState> connectionStateSubscriber;
    private TestObserver<StrategyRunData> runDataSubscriber;
    private final String cacheDirectory = "cacheDirectory";
    private final BufferedImage bufferedImage = new BufferedImage(2, 2, 2);
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
    public void systemListenerIsInitialized() {
        verify(clientMock).setSystemListener(isA(JFSystemListener.class));
    }

    @Test
    public void loginCompletableIsValid() {
        assertThat(clientUtil.login(loginCredentials), instanceOf(Completable.class));
    }

    @Test
    public void logoutCompletableIsValid() {
        assertThat(clientUtil.logout(), instanceOf(Completable.class));
    }

    @Test
    public void pinCaptchaForAWTCallsOnClient() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenReturn(bufferedImage);

        final Optional<BufferedImage> maybeImage = clientUtil.pinCaptchaForAWT(jnlpAddress);

        assertThat(maybeImage.get(), equalTo(bufferedImage));
        verify(clientMock).getCaptchaImage(jnlpAddress);
    }

    @Test
    public void pinCaptchaForAWTReturnsEmptyOptionalWhenClientThrows() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenThrow(jfException);

        final Optional<BufferedImage> maybeImage = clientUtil.pinCaptchaForAWT(jnlpAddress);

        assertFalse(maybeImage.isPresent());
    }

    @Test
    public void pinCaptchaForJavaFXCallsOnClient() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenReturn(bufferedImage);

        final Optional<Image> maybeImage = clientUtil.pinCaptchaForJavaFX(jnlpAddress);

        assertThat(maybeImage.get(), instanceOf(WritableImage.class));
        verify(clientMock).getCaptchaImage(jnlpAddress);
    }

    @Test
    public void pinCaptchaForJavaFXReturnsEmptyOptionalWhenClientThrows() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenThrow(jfException);

        final Optional<Image> maybeImage = clientUtil.pinCaptchaForJavaFX(jnlpAddress);

        assertFalse(maybeImage.isPresent());
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
}
