package com.jforex.programming.client;

import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFHotObservable;

import com.dukascopy.api.system.ISystemListener;

import rx.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFHotObservable<StrategyRunData> strategyRunDataPublisher = new JFHotObservable<>();
    private final JFHotObservable<ConnectionState> connectionStatePublisher = new JFHotObservable<>();

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
