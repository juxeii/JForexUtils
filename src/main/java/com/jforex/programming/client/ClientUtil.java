package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;

import io.reactivex.Completable;
import io.reactivex.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class ClientUtil {

    private final IClient client;
    private AuthentificationUtil authentificationUtil;
    private final JFSystemListener jfSystemListener = new JFSystemListener();

    private static final Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client,
                      final String cacheDirectory) {
        this.client = checkNotNull(client);
        initCacheDirectory(checkNotNull(cacheDirectory));
        initSystemListener();
        initAuthentification();
        initConnectionKeeper();
    }

    private void initCacheDirectory(final String cacheDirectory) {
        final File cacheDirectoryFile = new File(cacheDirectory);
        client.setCacheDirectory(cacheDirectoryFile);
        logger.debug("Setting of cache directory " + cacheDirectory + " for client done.");
    }

    private final void initSystemListener() {
        client.setSystemListener(jfSystemListener);
    }

    private final void initAuthentification() {
        authentificationUtil = new AuthentificationUtil(client, observeConnectionState());
    }

    private final void initConnectionKeeper() {
        observeConnectionState().subscribe(connectionState -> {
            logger.debug(connectionState + " message received.");
            if (connectionState == ConnectionState.DISCONNECTED
                    && authentificationUtil.loginState() == LoginState.LOGGED_IN) {
                logger.warn("Connection lost! Try to reconnect...");
                client.reconnect();
            }
        });
    }

    public final JFSystemListener jfSystemListener() {
        return jfSystemListener;
    }

    public final AuthentificationUtil authentificationUtil() {
        return authentificationUtil;
    }

    private final Observable<ConnectionState> observeConnectionState() {
        return jfSystemListener.observeConnectionState();
    }

    public final Completable observeLogin(final LoginCredentials loginCredentials) {
        return authentificationUtil.loginCompletable(checkNotNull(loginCredentials));
    }

    public final Optional<BufferedImage> pinCaptchaForAWT(final String jnlpAddress) {
        try {
            return Optional.of(client.getCaptchaImage(checkNotNull(jnlpAddress)));
        } catch (final Exception e) {
            logger.error("Error while retreiving pin captcha! " + e.getMessage());
            return Optional.empty();
        }
    }

    public final Optional<Image> pinCaptchaForJavaFX(final String jnlpAddress) {
        final Optional<BufferedImage> captcha = pinCaptchaForAWT(checkNotNull(jnlpAddress));
        return captcha.isPresent()
                ? Optional.of(SwingFXUtils.toFXImage(captcha.get(), null))
                : Optional.empty();
    }
}
