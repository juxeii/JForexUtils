package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionKeeper;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.misc.JFHotPublisher;

import io.reactivex.Observable;

public final class ClientUtil {

    private final IClient client;
    private final Authentification authentification;
    private final JFSystemListener jfSystemListener = new JFSystemListener();
    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private ConnectionKeeper connectionKeeper;
    private final PinCaptcha pinCaptcha;

    private static final Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client,
                      final String cacheDirectory) {
        checkNotNull(client);
        checkNotNull(cacheDirectory);

        this.client = client;
        initCacheDirectory(cacheDirectory);
        client.setSystemListener(jfSystemListener);
        authentification = new Authentification(client, loginStatePublisher);
        initConnectionKeeper();
        pinCaptcha = new PinCaptcha(client);
    }

    private void initCacheDirectory(final String cacheDirectoryPath) {
        final File cacheDirectoryFile = new File(cacheDirectoryPath);
        client.setCacheDirectory(cacheDirectoryFile);
        logger.debug("Setting of cache directory " + cacheDirectoryPath + " done.");
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

    public ConnectionKeeper connectionKeeper() {
        return connectionKeeper;
    }

    public final Authentification authentification() {
        return authentification;
    }

    public final PinCaptcha pinCaptcha() {
        return pinCaptcha;
    }
}
