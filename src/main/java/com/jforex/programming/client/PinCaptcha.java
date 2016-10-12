package com.jforex.programming.client;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class PinCaptcha {

    private final IClient client;

    private static final Logger logger = LogManager.getLogger(PinCaptcha.class);

    public PinCaptcha(final IClient client) {
        this.client = client;
    }

    public final Optional<BufferedImage> forAWT(final String jnlpAddress) {
        try {
            return Optional.of(client.getCaptchaImage(jnlpAddress));
        } catch (final Exception e) {
            logger.error("Exception while retreiving pin captcha! " + e.getMessage());
            return Optional.empty();
        }
    }

    public final Optional<Image> forJavaFX(final String jnlpAddress) {
        final Optional<BufferedImage> captcha = forAWT(jnlpAddress);
        return captcha.isPresent()
                ? Optional.of(SwingFXUtils.toFXImage(captcha.get(), null))
                : Optional.empty();
    }
}
