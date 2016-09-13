package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFHotObservable;

import io.reactivex.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFHotObservable<StrategyRunData> strategyRunDataObservable = new JFHotObservable<>();
    private final JFHotObservable<ConnectionState> connectionStateObservable = new JFHotObservable<>();

    public final Observable<StrategyRunData> observeStrategyRunData() {
        return strategyRunDataObservable.observable();
    }

    public final Observable<ConnectionState> observeConnectionState() {
        return connectionStateObservable.observable();
    }

    @Override
    public final void onStart(final long processId) {
        strategyRunDataObservable.onNext(new StrategyRunData(processId, StrategyRunState.STARTED));
    }

    @Override
    public final void onStop(final long processId) {
        strategyRunDataObservable.onNext(new StrategyRunData(processId, StrategyRunState.STOPPED));
    }

    @Override
    public final void onConnect() {
        connectionStateObservable.onNext(ConnectionState.CONNECTED);
    }

    @Override
    public final void onDisconnect() {
        connectionStateObservable.onNext(ConnectionState.DISCONNECTED);
    }
}
