package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionKeeper;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.misc.JFHotPublisher;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class ClientUtil {

    private final IClient client;
    private Authentification authentification;
    private final JFSystemListener jfSystemListener = new JFSystemListener();
    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private ConnectionKeeper connectionKeeper;

    private static final Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client,
                      final String cacheDirectory) {
        checkNotNull(client);
        checkNotNull(cacheDirectory);

        this.client = client;
        initCacheDirectory(cacheDirectory);
        initSystemListener();
        initAuthentification();
        initConnectionKeeper();
    }

    private void initCacheDirectory(final String cacheDirectoryPath) {
        final File cacheDirectoryFile = new File(cacheDirectoryPath);
        client.setCacheDirectory(cacheDirectoryFile);
        logger.debug("Setting of cache directory " + cacheDirectoryPath + " done.");
    }

    private final void initSystemListener() {
        client.setSystemListener(jfSystemListener);
    }

    private final void initAuthentification() {
        authentification = new Authentification(client, loginStatePublisher);
    }

    private final void initConnectionKeeper() {
        connectionKeeper = new ConnectionKeeper(client,
                                                observeConnectionState(),
                                                loginStatePublisher.observable());
    }

    public Observable<ConnectionState> observeConnectionState() {
        return jfSystemListener.observeConnectionState();
    }

    public Observable<StrategyRunData> observeStrategyRunData() {
        return jfSystemListener.observeStrategyRunData();
    }

    public final Completable login(final LoginCredentials loginCredentials) {
        checkNotNull(loginCredentials);

        return authentification.login(loginCredentials);
    }

    public final Completable logout() {
        return authentification.logout();
    }

    public void setReconnectComposer(final ObservableTransformer<ConnectionState, ConnectionState> reconnectComposer) {
        connectionKeeper.setReconnectComposer(reconnectComposer);
    }

    public final Optional<BufferedImage> pinCaptchaForAWT(final String jnlpAddress) {
        checkNotNull(jnlpAddress);

        try {
            return Optional.of(client.getCaptchaImage(jnlpAddress));
        } catch (final Exception e) {
            logger.error("Exception while retreiving pin captcha! " + e.getMessage());
            return Optional.empty();
        }
    }

    public final Optional<Image> pinCaptchaForJavaFX(final String jnlpAddress) {
        checkNotNull(jnlpAddress);

        final Optional<BufferedImage> captcha = pinCaptchaForAWT(jnlpAddress);
        return captcha.isPresent()
                ? Optional.of(SwingFXUtils.toFXImage(captcha.get(), null))
                : Optional.empty();
    }
}
