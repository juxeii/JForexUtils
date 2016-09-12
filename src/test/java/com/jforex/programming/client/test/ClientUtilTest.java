package com.jforex.programming.client.test;

import static info.solidsoft.mockito.java8.LambdaMatcher.argLambda;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import io.reactivex.observers.TestObserver;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

@RunWith(HierarchicalContextRunner.class)
public class ClientUtilTest extends CommonUtilForTest {

    private ClientUtil clientUtil;

    private JFSystemListener jfSystemListener;
    private final TestObserver<ConnectionState> connectionStateSubscriber = TestObserver.create();
    private final TestObserver<StrategyRunData> runDataSubscriber = TestObserver.create();
    private final String cacheDirectory = "cacheDirectory";
    private final BufferedImage bufferedImage = new BufferedImage(2, 2, 2);

    @Before
    public void setUp() {
        clientUtil = new ClientUtil(clientMock, cacheDirectory);

        clientUtil
            .connectionStateObservable()
            .subscribe(connectionStateSubscriber);
        clientUtil
            .strategyInfoObservable()
            .subscribe(runDataSubscriber);
        jfSystemListener = clientUtil.systemListener();
    }

    @Test
    public void cacheDirectoryIsInitialized() {
        verify(clientMock)
            .setCacheDirectory(argLambda(file -> file.getName().equals(cacheDirectory)));
    }

    @Test
    public void systemListenerIsInitialized() {
        verify(clientMock).setSystemListener(isA(JFSystemListener.class));
    }

    @Test
    public void authentificationUtilIsValid() {
        assertNotNull(clientUtil.authentificationUtil());
    }

    @Test
    public void loginCompletableIsValid() {
        assertThat(clientUtil.loginCompletable(loginCredentials), instanceOf(Completable.class));
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
    public void noReconnectIsDoneWhenNotLoggedIn() {
        jfSystemListener.onDisconnect();

        verify(clientMock, never()).reconnect();
    }

    public class AfterConnectMessage {

        @Before
        public void setUp() {
            jfSystemListener.onConnect();
        }

        @Test
        public void connectMessageIsPublished() {
            connectionStateSubscriber.assertNoErrors();
            connectionStateSubscriber.assertValueCount(1);

            assertThat(getOnNextEvent(connectionStateSubscriber, 0),
                       equalTo(ConnectionState.CONNECTED));
        }

        public class AfterDisConnectMessage {

            @Before
            public void setUp() {
                jfSystemListener.onDisconnect();
            }

            @Test
            public void disConnectMessageIsPublished() {
                connectionStateSubscriber.assertNoErrors();
                connectionStateSubscriber.assertValueCount(2);

                assertThat(getOnNextEvent(connectionStateSubscriber, 1),
                           equalTo(ConnectionState.DISCONNECTED));
            }

            @Test
            public void reconnectOnClientIsDone() {
                verify(clientMock).reconnect();
            }
        }
    }

    public class AfterStrategyStart {

        private final long processID = 42L;

        private void assertRunData(final StrategyRunState strategyRunState,
                                   final int index) {

            assertThat(getOnNextEvent(runDataSubscriber, index).state(),
                       equalTo(strategyRunState));
            assertThat(getOnNextEvent(runDataSubscriber, index).processID(),
                       equalTo(processID));
        }

        @Before
        public void setUp() {
            jfSystemListener.onStart(processID);
        }

        @Test
        public void strategyStartIsPublished() {
            runDataSubscriber.assertNoErrors();
            runDataSubscriber.assertValueCount(1);

            assertRunData(StrategyRunState.STARTED, 0);
        }

        public class AfterStrategyStop {

            @Before
            public void setUp() {
                jfSystemListener.onStop(42L);
            }

            @Test
            public void strategyStopIsPublished() {
                runDataSubscriber.assertNoErrors();
                runDataSubscriber.assertValueCount(2);

                assertRunData(StrategyRunState.STOPPED, 1);
            }
        }
    }
}
