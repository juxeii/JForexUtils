package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFHotObservable;

import io.reactivex.Observable;

public final class JFSystemListener implements ISystemListener {

    private final JFHotObservable<StrategyRunData> strategyRunDataSubject = new JFHotObservable<>();
    private final JFHotObservable<ConnectionState> connectionStateSubject = new JFHotObservable<>();

    public final Observable<StrategyRunData> strategyRunDataObservable() {
        return strategyRunDataSubject.observable();
    }

    public final Observable<ConnectionState> connectionStateObservable() {
        return connectionStateSubject.observable();
    }

    @Override
    public final void onStart(final long processId) {
        strategyRunDataSubject.onNext(new StrategyRunData(processId, StrategyRunState.STARTED));
    }

    @Override
    public final void onStop(final long processId) {
        strategyRunDataSubject.onNext(new StrategyRunData(processId, StrategyRunState.STOPPED));
    }

    @Override
    public final void onConnect() {
        connectionStateSubject.onNext(ConnectionState.CONNECTED);
    }

    @Override
    public final void onDisconnect() {
        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
    }
}
