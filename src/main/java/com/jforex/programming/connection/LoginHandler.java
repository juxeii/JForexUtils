package com.jforex.programming.connection;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Supplier;
import com.jforex.programming.misc.JFEventPublisherForRx;

import com.dukascopy.api.system.IClient;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import rx.Observable;

public class LoginHandler {

    private final Authentification authentification;
    private final IClient client;
    private final JFEventPublisherForRx<LoginState> loginStatePublisher = new JFEventPublisherForRx<>();
    private Supplier<LoginResult> latestLoginCall;
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final IClient client) {
        this.client = client;
        authentification = new Authentification(client);
    }

    public void login(final String jnlp,
                      final String username,
                      final String password) {
        latestLoginCall = () -> authentification.login(jnlp, username, password);
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void loginWithPin(final String jnlp,
                             final String username,
                             final String password,
                             final String pin) {
        latestLoginCall = () -> authentification.loginWithPin(jnlp, username, password, pin);
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void relogin() {
        final LoginResult loginResult = latestLoginCall.get();
        evaluateLoginResult(loginResult);
    }

    public void reconnect() {
        client.reconnect();
    }

    private void evaluateLoginResult(final LoginResult loginResult) {
        if (loginResult.type() == LoginResultType.LOGGED_IN) {
            while (!client.isConnected()) {
                logger.debug("Waiting for client to connect...");
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            logger.debug("Client connected.");
            updateState(LoginState.LOGGED_IN);
        } else {
            logger.error("Exception occured during login! " + loginResult.exceptionOpt().get());
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
