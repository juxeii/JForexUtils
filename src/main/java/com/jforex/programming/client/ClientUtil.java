package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.connection.Reconnector;
import com.jforex.programming.connection.UserConnection;
import com.jforex.programming.rx.JFHotPublisher;

import io.reactivex.Observable;

public class ClientUtil {

    private final IClient client;
    private final Authentification authentification;
    private final JFSystemListener jfSystemListener = new JFSystemListener();
    private final JFHotPublisher<LoginState> loginStatePublisher = new JFHotPublisher<>();
    private final UserConnection connectionMonitor;
    private final Reconnector reconnector;
    private final PinCaptcha pinCaptcha;

    private static final Logger logger = LogManager.getLogger(ClientUtil.class);

    public ClientUtil(final IClient client,
                      final String cacheDirectory) {
        checkNotNull(client);
        checkNotNull(cacheDirectory);

        this.client = client;
        pinCaptcha = new PinCaptcha(client);
        authentification = new Authentification(client,
                                                observeConnectionState(),
                                                loginStatePublisher);

        initCacheDirectory(cacheDirectory);
        client.setSystemListener(jfSystemListener);
        connectionMonitor = new UserConnection(observeConnectionState(), loginStatePublisher.observable());
        reconnector = new Reconnector(client,
                                      authentification,
                                      connectionMonitor);
    }

    private void initCacheDirectory(final String cacheDirectoryPath) {
        final File cacheDirectoryFile = new File(cacheDirectoryPath);
        client.setCacheDirectory(cacheDirectoryFile);
        logger.debug("Setting of cache directory " + cacheDirectoryPath + " done.");
    }

    public IClient client() {
        return client;
    }

    public Observable<ConnectionState> observeConnectionState() {
        return jfSystemListener.observeConnectionState();
    }

    public Observable<StrategyRunData> observeStrategyRunData() {
        return jfSystemListener.observeStrategyRunData();
    }

    public Reconnector reconnector() {
        return reconnector;
    }

    public Authentification authentification() {
        return authentification;
    }

    public PinCaptcha pinCaptcha() {
        return pinCaptcha;
    }
}
