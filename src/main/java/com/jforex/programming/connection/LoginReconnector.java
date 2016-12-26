package com.jforex.programming.connection;

import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Completable;

public class LoginReconnector {

    private final Authentification authentification;
    private final ConnectionMonitor connectionMonitor;
    private final ReconnectParams reconnectParams;

    public LoginReconnector(final Authentification authentification,
                            final ConnectionMonitor connectionMonitor,
                            final ReconnectParams reconnectParams) {
        this.authentification = authentification;
        this.connectionMonitor = connectionMonitor;
        this.reconnectParams = reconnectParams;
    }

    public Completable strategy() {
        return reconnectParams.noOfRelogins() > 0
                ? authentification
                    .login(reconnectParams.loginCredentials())
                    .andThen(connectionMonitor.observe())
                    .retryWhen(RxUtil.retryWithDelay(new RetryParams(reconnectParams.noOfRelogins(),
                                                                     reconnectParams.reloginDelayFunction())))
                    .ignoreElements()
                : Completable.complete();
    }
}
