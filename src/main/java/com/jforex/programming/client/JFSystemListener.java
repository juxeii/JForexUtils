package com.jforex.programming.client;

import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFObservable;

import com.dukascopy.api.system.ISystemListener;

import rx.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFObservable<StrategyRunData> strategyRunDataPublisher = new JFObservable<>();
    private final JFObservable<ConnectionState> connectionStatePublisher = new JFObservable<>();

    public final Observable<StrategyRunData> strategyObs() {
        return strategyRunDataPublisher.get();
    }

    public final Observable<ConnectionState> connectionObs() {
        return connectionStatePublisher.get();
    }

    @Override
    public final void onStart(final long processId) {
        strategyRunDataPublisher.onNext(new StrategyRunData(processId, StrategyRunState.STARTED));
    }

    @Override
    public final void onStop(final long processId) {
        strategyRunDataPublisher.onNext(new StrategyRunData(processId, StrategyRunState.STOPPED));
    }

    @Override
    public final void onConnect() {
        connectionStatePublisher.onNext(ConnectionState.CONNECTED);
    }

    @Override
    public final void onDisconnect() {
        connectionStatePublisher.onNext(ConnectionState.DISCONNECTED);
    }
}
