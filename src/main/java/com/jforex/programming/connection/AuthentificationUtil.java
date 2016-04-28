package com.jforex.programming.connection;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.google.common.base.Supplier;
import com.jforex.programming.misc.JFEventPublisherForRx;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import rx.Observable;

public class AuthentificationUtil {

    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private Supplier<Optional<Exception>> latestLoginCall;
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(AuthentificationUtil.class);

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;
        connectionStateObs.subscribe(this::onConnectionState);
    }

    public final synchronized Optional<Exception> login(final String jnlpAddress,
                                                        final String username,
                                                        final String password) {
        latestLoginCall = () -> connectClient(jnlpAddress, username, password, "");
        return latestLoginCall.get();
    }

    public final synchronized Optional<Exception> loginWithPin(final String jnlpAddress,
                                                               final String username,
                                                               final String password,
                                                               final String pin) {
        latestLoginCall = () -> connectClient(jnlpAddress, username, password, pin);
        return latestLoginCall.get();
    }

    private Optional<Exception> connectClient(final String jnlpAddress,
                                              final String username,
                                              final String password,
                                              final String pin) {
        try {
            if (pin.isEmpty())
                client.connect(jnlpAddress, username, password);
            else
                client.connect(jnlpAddress, username, password, pin);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public void logout() {
        client.disconnect();
        updateState(LoginState.LOGGED_OUT);
    }

    public void relogin() {
        if (latestLoginCall != null)
            processLoginCall();
    }

    public Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    private void onConnectionState(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED && loginState == LoginState.LOGGED_OUT)
            updateState(LoginState.LOGGED_IN);
    }

    private void processLoginCall() {
        final Optional<Exception> exceptionOpt = latestLoginCall.get();
        if (exceptionOpt.isPresent())
            logger.error("Exception occured during login! " + exceptionOpt.get().getMessage());
    }

    private void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public LoginState state() {
        return loginState;
    }

    public void reconnect() {
        client.reconnect();
    }

    public Optional<BufferedImage> pinCaptchaForAWT(final String jnlpAddress) {
        try {
            return Optional.of(client.getCaptchaImage(jnlpAddress));
        } catch (final Exception e) {
            logger.error("Error while retreiving pin captcha! " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Image> pinCaptchaForJavaFX(final String jnlpAddress) {
        final Optional<BufferedImage> captcha = pinCaptchaForAWT(jnlpAddress);
        return captcha.isPresent()
                ? Optional.of(SwingFXUtils.toFXImage(captcha.get(), null))
                : Optional.empty();
    }
}
