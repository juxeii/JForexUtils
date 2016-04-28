package com.jforex.programming.misc;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;

import com.dukascopy.api.system.IClient;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import rx.Observable;

public final class ClientUtil {

    private final IClient client;

    private JFSystemListener jfSystemListener;
    private AuthentificationUtil authentificationUtil;

    private final static Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client) {
        this.client = client;

        initSystemListener();
        initAuthentification();
    }

    private final void initSystemListener() {
        jfSystemListener = new JFSystemListener();
        client.setSystemListener(jfSystemListener);
    }

    private final void initAuthentification() {
        authentificationUtil = new AuthentificationUtil(client, connectionStateObs());
    }

    public final Observable<ConnectionState> connectionStateObs() {
        return jfSystemListener.connectionObs();
    }

    public final Observable<StrategyInfo> strategyInfoObs() {
        return jfSystemListener.strategyObs();
    }

    public final Optional<BufferedImage> pinCaptchaForAWT(final String jnlpAddress) {
        try {
            return Optional.of(client.getCaptchaImage(jnlpAddress));
        } catch (final Exception e) {
            logger.error("Error while retreiving pin captcha! " + e.getMessage());
            return Optional.empty();
        }
    }

    public final Optional<Image> pinCaptchaForJavaFX(final String jnlpAddress) {
        final Optional<BufferedImage> captcha = pinCaptchaForAWT(jnlpAddress);
        return captcha.isPresent()
                ? Optional.of(SwingFXUtils.toFXImage(captcha.get(), null))
                : Optional.empty();
    }

    public final AuthentificationUtil authentificationUtil() {
        return authentificationUtil;
    }
}
