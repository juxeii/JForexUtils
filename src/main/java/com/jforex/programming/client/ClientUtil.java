package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionMonitor;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LightReconnector;
import com.jforex.programming.connection.LoginReconnector;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.connection.ReconnectParams;
import com.jforex.programming.connection.Reconnector;
import com.jforex.programming.misc.JFHotPublisher;

import io.reactivex.Completable;
import io.reactivex.Observable;

public final class ClientUtil {

    private final IClient client;
    private final Authentification authentification;
    private final JFSystemListener jfSystemListener = new JFSystemListener();
    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private final ConnectionMonitor connectionMonitor;
    private final Reconnector reconnector;
    private final PinCaptcha pinCaptcha;

    private static final Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client,
                      final String cacheDirectory) {
        checkNotNull(client);
        checkNotNull(cacheDirectory);

        this.client = client;
        pinCaptcha = new PinCaptcha(client);
        authentification = new Authentification(client, loginStatePublisher);

        initCacheDirectory(cacheDirectory);
        client.setSystemListener(jfSystemListener);
        connectionMonitor = new ConnectionMonitor(observeConnectionState(), loginStatePublisher.observable());
        reconnector = new Reconnector(connectionMonitor);
    }

    private void initCacheDirectory(final String cacheDirectoryPath) {
        final File cacheDirectoryFile = new File(cacheDirectoryPath);
        client.setCacheDirectory(cacheDirectoryFile);
        logger.debug("Setting of cache directory " + cacheDirectoryPath + " done.");
    }

    public Observable<ConnectionState> observeConnectionState() {
        return jfSystemListener.observeConnectionState();
    }

    public Observable<StrategyRunData> observeStrategyRunData() {
        return jfSystemListener.observeStrategyRunData();
    }

    public void setReconnectParams(final ReconnectParams reconnectParams) {
        final LightReconnector lightReconnector = new LightReconnector(client,
                                                                       connectionMonitor,
                                                                       reconnectParams);
        final LoginReconnector loginReconnector = new LoginReconnector(authentification,
                                                                       connectionMonitor,
                                                                       reconnectParams);
        final Completable reconnectStrategy = lightReconnector
            .strategy()
            .onErrorResumeNext(err -> loginReconnector.strategy());
        reconnector.applyStrategy(reconnectStrategy);
    }

    public final Authentification authentification() {
        return authentification;
    }

    public final PinCaptcha pinCaptcha() {
        return pinCaptcha;
    }
}
