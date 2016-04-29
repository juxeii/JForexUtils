package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFEventPublisherForRx;

import rx.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFEventPublisherForRx<StrategyInfo> strategyInfoPublisher = new JFEventPublisherForRx<>();
    private final JFEventPublisherForRx<ConnectionState> connectionStatePublisher = new JFEventPublisherForRx<>();

    public final Observable<StrategyInfo> strategyObs() {
        return strategyInfoPublisher.observable();
    }

    public final Observable<ConnectionState> connectionObs() {
        return connectionStatePublisher.observable();
    }

    @Override
    public final void onStart(final long processId) {
        strategyInfoPublisher.onJFEvent(new StrategyInfo(processId, StrategyState.STARTED));
    }

    @Override
    public final void onStop(final long processId) {
        strategyInfoPublisher.onJFEvent(new StrategyInfo(processId, StrategyState.STOPPED));
    }

    @Override
    public final void onConnect() {
        connectionStatePublisher.onJFEvent(ConnectionState.CONNECTED);
    }

    @Override
    public final void onDisconnect() {
        connectionStatePublisher.onJFEvent(ConnectionState.DISCONNECTED);
    }
}
