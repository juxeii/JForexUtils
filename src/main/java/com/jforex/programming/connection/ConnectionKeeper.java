package com.jforex.programming.connection;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Completable.CompletableSubscriber;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public final class ConnectionKeeper {

    private Completable lightReconnectCompletable;
    private Completable reloginCompletable;
    private Scheduler scheduler = Schedulers.computation();
    private final AuthentificationUtil authentificationUtil;
    private final LoginCredentials loginCredentials;
    private final StateMachineConfig<FSMState, ConnectionState> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<FSMState, ConnectionState> fsm = new StateMachine<>(FSMState.IDLE, fsmConfig);

    private enum FSMState {
        IDLE,
        RECONNECTING
    }

    private final class ReconnectException extends Exception {
        private final static long serialVersionUID = 1L;
    }

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final Observable<ConnectionState> connectionStateObs,
                            final AuthentificationUtil authentificationUtil,
                            final LoginCredentials loginCredentials) {
        this.authentificationUtil = authentificationUtil;
        this.loginCredentials = loginCredentials;

        intObservables(connectionStateObs);
        configureFSM();
    }

    private final void intObservables(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(this::onConnectionStateUpdate);

        lightReconnectCompletable = Completable.create(subscriber -> {
            logger.debug("Try to do a light reconnection...");
            authentificationUtil.reconnect();
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
        connectionStateObs.take(1)
                .subscribe(connectionState -> {
                    if (connectionState == ConnectionState.CONNECTED)
                        subscriber.onCompleted();
                    else
                        subscriber.onError(new ReconnectException());
                });
    }

    private final void configureFSM() {
        fsmConfig.configure(FSMState.IDLE)
                .permitDynamic(ConnectionState.DISCONNECTED, () -> {
                    return authentificationUtil.state() == LoginState.LOGGED_IN
                            ? FSMState.RECONNECTING
                            : FSMState.IDLE;
                })
                .ignore(ConnectionState.CONNECTED);

        fsmConfig.configure(FSMState.RECONNECTING)
                .onEntry(() -> startReconnectStrategy())
                .permit(ConnectionState.CONNECTED, FSMState.IDLE)
                .ignore(ConnectionState.DISCONNECTED);
    }

    public void setReloginTimeOutScheduler(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private final void onConnectionStateUpdate(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED)
            logger.debug("Connect message received.");
        else
            logger.debug("Disconnect message received!");
        fsm.fire(connectionState);
    }

    private final void startReconnectStrategy() {
        lightReconnectCompletable
                .retry(platformSettings.noOfLightReconnects() - 1)
                .subscribe(exc -> {
                    logger.debug("Light reconnect failed, try to relogin!");
                    startReloginStrategy();
                }, () -> logger.debug("Light reconnect successful!"));
    }

    private final void startReloginStrategy() {
        reloginCompletable
                .timeout(platformSettings.logintimeoutseconds(), TimeUnit.SECONDS, scheduler)
                .retry()
                .subscribe(exc -> logger.debug("Relogin failed!"),
                           () -> logger.debug("Relogin successful!"));
    }
}
