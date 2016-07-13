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
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import rx.Completable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class ClientUtilTest extends CommonUtilForTest {

    private ClientUtil clientUtil;

    private JFSystemListener jfSystemListener;
    private final TestSubscriber<ConnectionState> connectionStateSubscriber =
            new TestSubscriber<>();
    private final TestSubscriber<StrategyRunData> runDataSubscriber =
            new TestSubscriber<>();
    private final String cacheDirectory = "cacheDirectory";
    private final BufferedImage bufferedImage = new BufferedImage(2, 2, 2);

    @Before
    public void setUp() {
        initCommonTestFramework();

        clientUtil = new ClientUtil(clientMock, cacheDirectory);
        jfSystemListener = clientUtil.systemListener();
        clientUtil.connectionStateObservable().subscribe(connectionStateSubscriber);
        clientUtil.strategyInfoObservable().subscribe(runDataSubscriber);
    }

    @Test
    public void cacheDirectoryIsInitialized() {
        verify(clientMock)
                .setCacheDirectory(argLambda(v -> v.getName().equals(cacheDirectory)));
    }

    @Test
    public void systemListenerIsInitialized() {
        verify(clientMock).setSystemListener(any(JFSystemListener.class));
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
            jfSystemListener.onStart(42L);
        }

        @Test
        public void connectMessageIsPublished() {
            connectionStateSubscriber.assertNoErrors();
            connectionStateSubscriber.assertValueCount(1);

            assertThat(connectionStateSubscriber.getOnNextEvents().get(0),
                       equalTo(ConnectionState.CONNECTED));
        }

        @Test
        public void strategyStartIsPublished() {
            runDataSubscriber.assertNoErrors();
            runDataSubscriber.assertValueCount(1);

            assertThat(runDataSubscriber.getOnNextEvents().get(0).processID(),
                       equalTo(42L));
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

                assertThat(connectionStateSubscriber.getOnNextEvents().get(1),
                           equalTo(ConnectionState.DISCONNECTED));
            }

            @Test
            public void reconnectOnClientIsDone() {
                verify(clientMock).reconnect();
            }
        }
    }
}
