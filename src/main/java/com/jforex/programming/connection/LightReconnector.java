package com.jforex.programming.connection;

import com.dukascopy.api.system.IClient;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Completable;

public class LightReconnector {

    private final IClient client;
    private final ConnectionMonitor connectionMonitor;
    private final ReconnectParams reconnectParams;

    public LightReconnector(final IClient client,
                            final ConnectionMonitor connectionMonitor,
                            final ReconnectParams reconnectParams) {
        this.client = client;
        this.connectionMonitor = connectionMonitor;
        this.reconnectParams = reconnectParams;
    }

    public Completable strategy() {
        return reconnectParams.noOfLightReconnects() > 0
                ? Completable
                    .fromAction(() -> client.reconnect())
                    .andThen(connectionMonitor.observe())
                    .retryWhen(RxUtil.retryWithDelay(new RetryParams(reconnectParams.noOfLightReconnects(),
                                                                     reconnectParams.lightReconnectDelayFunction())))
                    .ignoreElements()
                : Completable.complete();
    }
}
