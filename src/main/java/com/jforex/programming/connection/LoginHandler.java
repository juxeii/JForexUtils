package com.jforex.programming.connection;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Supplier;
import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.system.IClient;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import rx.Observable;

public class LoginHandler {

    private final Login authentification;
    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private Supplier<Optional<Exception>> latestLoginCall;
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final IClient client) {
        this.client = client;
        authentification = new Login(client);
    }

    public void login(final String jnlp,
                      final String username,
                      final String password) {
        latestLoginCall = () -> authentification.withoutPin(jnlp, username, password);
        final Optional<Exception> exceptionOpt = latestLoginCall.get();
        evaluateLoginResult(exceptionOpt);
    }

    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password,
                             final String pin) {
        latestLoginCall = () -> authentification.withPin(jnlp, username, password, pin);
        final Optional<Exception> exceptionOpt = latestLoginCall.get();
        evaluateLoginResult(exceptionOpt);
    }

    public void relogin() {
        final Optional<Exception> exceptionOpt = latestLoginCall.get();
        evaluateLoginResult(exceptionOpt);
    }

    public void reconnect() {
        client.reconnect();
    }

    private void evaluateLoginResult(final Optional<Exception> exceptionOpt) {
        if (!exceptionOpt.isPresent()) {
            while (!client.isConnected()) {
                logger.debug("Waiting for client to connect...");
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e) {}
            }
            logger.debug("Client connected.");
            updateState(LoginState.LOGGED_IN);
        } else {
            logger.error("Exception occured during login! " + exceptionOpt.get());
            System.exit(0);
        }
    }

    private void updateState(final LoginState loginState) {
        this.loginState = loginState;
        loginStatePublisher.onJFEvent(loginState);
    }

    public LoginState state() {
        return loginState;
    }

    public Observable<LoginState> loginStateObs() {
        return loginStatePublisher.observable();
    }

    public void logout() {
        authentification.logout();
        updateState(LoginState.LOGGED_OUT);
    }

    public BufferedImage pinCaptchaForAWT(final String jnlpUrl) {
        try {
            return client.getCaptchaImage(jnlpUrl);
        } catch (final Exception e) {
            logger.error("Error while retreiving pin captcha! " + e.getMessage());
            return null;
        }
    }

    public Image pinCaptchaForJavaFX(final String jnlpUrl) {
        final BufferedImage captcha = pinCaptchaForAWT(jnlpUrl);
        return captcha != null ? SwingFXUtils.toFXImage(captcha, null) : null;
    }
}
