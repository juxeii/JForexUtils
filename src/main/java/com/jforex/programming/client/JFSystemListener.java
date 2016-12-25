package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.rx.JFHotPublisher;

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
        publishStrategyRunData(StrategyRunState.STARTED, processId);
    }

    @Override
    public final void onStop(final long processId) {
        publishStrategyRunData(StrategyRunState.STOPPED, processId);
    }

    private final void publishStrategyRunData(final StrategyRunState strategyRunState,
                                              final long processId) {
        strategyRunDataPublisher.onNext(new StrategyRunData(processId, strategyRunState));
    }

    @Override
    public final void onConnect() {
        publishConnectionState(ConnectionState.CONNECTED);
    }

    @Override
    public final void onDisconnect() {
        publishConnectionState(ConnectionState.DISCONNECTED);
    }

    private final void publishConnectionState(final ConnectionState connectionState) {
        connectionStatePublisher.onNext(connectionState);
    }
}
