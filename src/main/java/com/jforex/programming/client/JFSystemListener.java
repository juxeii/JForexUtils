package com.jforex.programming.client;

import com.dukascopy.api.system.ISystemListener;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.misc.JFHotSubject;

import io.reactivex.Flowable;

public final class JFSystemListener implements ISystemListener {

    private final JFHotSubject<StrategyRunData> strategyRunDataSubject = new JFHotSubject<>();
    private final JFHotSubject<ConnectionState> connectionStateSubject = new JFHotSubject<>();

    public final Flowable<StrategyRunData> strategyRunDataFlowable() {
        return strategyRunDataSubject.flowable();
    }

    public final Flowable<ConnectionState> connectionStateFlowable() {
        return connectionStateSubject.flowable();
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
