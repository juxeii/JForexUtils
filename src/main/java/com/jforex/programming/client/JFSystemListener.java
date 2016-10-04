package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFHotPublisher;

import io.reactivex.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFHotPublisher<StrategyRunData> strategyRunDataPublisher = new JFHotPublisher<>();
    private final JFHotPublisher<ConnectionState> connectionStatePublisher = new JFHotPublisher<>();

    public final Observable<StrategyRunData> observeStrategyRunData() {
        return strategyRunDataPublisher.observable();
    }

    public final Observable<ConnectionState> observeConnectionState() {
        return connectionStatePublisher.observable();
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
