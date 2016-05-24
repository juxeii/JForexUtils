package com.jforex.programming.connection;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.system.IClient;

import rx.Completable;
import rx.Completable.CompletableSubscriber;
import rx.Observable;

public final class ConnectionKeeper {

    private final IClient client;
    private Completable lightReconnectCompletable;
    private Completable reloginCompletable;
    private final AuthentificationUtil authentificationUtil;
    private final LoginCredentials loginCredentials;
    private final StateMachineConfig<FSMState, ConnectionState> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<FSMState, ConnectionState> fsm = new StateMachine<>(FSMState.IDLE, fsmConfig);

    private enum FSMState {
        IDLE,
        RECONNECTING
    }

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final IClient client,
                            final Observable<ConnectionState> connectionStateObs,
                            final AuthentificationUtil authentificationUtil,
                            final LoginCredentials loginCredentials) {
        this.client = client;
        this.authentificationUtil = authentificationUtil;
        this.loginCredentials = loginCredentials;

        intObservables(connectionStateObs);
        configureFSM();
    }

    private final void intObservables(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(this::onConnectionStateUpdate);

        lightReconnectCompletable = Completable.create(subscriber -> {
            logger.debug("Try to do a light reconnection...");
            client.reconnect();
            initNextConnectionStateObs(connectionStateObs, subscriber);
        });

        reloginCompletable = Completable.create(subscriber -> {
            logger.debug("Try to relogin...");
            authentificationUtil.login(loginCredentials);
            initNextConnectionStateObs(connectionStateObs, subscriber);
        });
    }

    private void initNextConnectionStateObs(final Observable<ConnectionState> connectionStateObs,
                                            final CompletableSubscriber subscriber) {
        connectionStateObs.subscribe(connectionState -> {
            if (connectionState == ConnectionState.CONNECTED || client.isConnected())
                subscriber.onCompleted();
            else
                subscriber.onError(new ConnectException());
        });
    }

    private final void configureFSM() {
        fsmConfig.configure(FSMState.IDLE)
                .permitDynamic(ConnectionState.DISCONNECTED, () -> {
                    if (authentificationUtil.loginState() == LoginState.LOGGED_IN && !client.isConnected())
                        return FSMState.RECONNECTING;
                    return FSMState.IDLE;
                })
                .ignore(ConnectionState.CONNECTED);

        fsmConfig.configure(FSMState.RECONNECTING)
                .onEntry(() -> startReconnectStrategy())
                .permit(ConnectionState.CONNECTED, FSMState.IDLE)
                .ignore(ConnectionState.DISCONNECTED);
    }

    private final void onConnectionStateUpdate(final ConnectionState connectionState) {
        logger.debug(connectionState + " message received!");
        fsm.fire(connectionState);
    }

    private final void startReconnectStrategy() {
        lightReconnectCompletable
                .retry(platformSettings.noOfLightReconnects() - 1)
                .subscribe(exc -> startReloginStrategy(),
                           () -> logger.debug("Light reconnect successful!"));
    }

    private final void startReloginStrategy() {
        logger.debug("Light reconnect failed, try to relogin!");
        reloginCompletable
                .timeout(platformSettings.logintimeoutseconds(), TimeUnit.SECONDS)
                .retry()
                .subscribe(exc -> logger.debug("Relogin failed!"),
                           () -> logger.debug("Relogin successful!"));
    }
}
