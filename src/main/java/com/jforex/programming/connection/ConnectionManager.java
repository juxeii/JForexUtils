package com.jforex.programming.connection;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.misc.RxUtil;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class ConnectionManager {

    private final ConnectionHandlerParams handlerParams;
    private final Authentification authentification;
    private final IClient client;
    private final Observable<ConnectionState> connectionStateObservable;
    private Observable<ConnectionState> reconnectStrategy;
    private ObservableTransformer<ConnectionState, ConnectionState> reconnectComposer;
    private LoginState currentLoginState = LoginState.LOGGED_OUT;

    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    public ConnectionManager(final ConnectionHandlerParams handlerParams,
                             final Authentification authentification,
                             final IClient client,
                             final Observable<ConnectionState> connectionStateObservable,
                             final Observable<LoginState> loginStateObservable) {
        this.handlerParams = handlerParams;
        this.authentification = authentification;
        this.client = client;
        this.connectionStateObservable = connectionStateObservable;

        monitorConnection();
        monitorLoginState(loginStateObservable);
        initReconnectStrategy();
    }

    private void monitorConnection() {
        connectionStateObservable
            .doOnNext(cs -> logger.debug("Received connection state update " + cs
                    + ". Current login state is " + currentLoginState))
            .takeUntil(this::isConnectionLostWhileLoggedIn)
            .doOnComplete(() -> {
                if (reconnectComposer != null)
                    startRetryStrategy();
            })
            .subscribe();
    }

    private void monitorLoginState(final Observable<LoginState> loginStateObservable) {
        loginStateObservable.subscribe(ls -> {
            logger.debug("Received login state update " + ls);
            currentLoginState = ls;
        });
    }

    private void initReconnectStrategy() {
        initLightReconnectStrategy();
        initReloginStrategy();
    }

    private void initLightReconnectStrategy() {
        reconnectStrategy = handlerParams.noOfLightReconnects() > 0
                ? Completable
                    .fromAction(() -> client.reconnect())
                    .andThen(observeConnectionStateWithError())
                : Observable.empty();
    }

    private void initReloginStrategy() {
        if (handlerParams.noOfRelogins() > 0) {
            final Observable<ConnectionState> login = authentification
                .login(handlerParams.loginCredentials())
                .andThen(observeConnectionStateWithError())
                .retryWhen(RxUtil.retryWhen(handlerParams.noOfRelogins(),
                                            handlerParams.reloginDelay(),
                                            TimeUnit.MILLISECONDS));
            reconnectStrategy = reconnectStrategy.concatWith(login);
        }
    }

    private final Observable<ConnectionState> observeConnectionStateWithError() {
        return connectionStateObservable.concatMap(cs -> isConnectionLostWhileLoggedIn(cs)
                ? Observable.error(new Exception("Connection to server lost while logged in"))
                : Observable.just(cs));
    }

    private void startRetryStrategy() {
        reconnectStrategy
            .doOnSubscribe(d -> logger.debug("Trying to reconnect..."))
            .compose(reconnectComposer)
            .take(1)
            .doAfterTerminate(this::monitorConnection)
            .subscribe(cs -> logger.debug("Connection successfully reestablished."),
                       e -> logger.debug("Failed to reconnect! " + e.getMessage()));
    }

    private boolean isConnectionLostWhileLoggedIn(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED
                && currentLoginState == LoginState.LOGGED_IN) {
            logger.warn("Connection to server lost while logged in!");
            return true;
        }
        return false;
    }
}
