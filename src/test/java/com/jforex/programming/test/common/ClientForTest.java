package com.jforex.programming.test.common;

import com.dukascopy.api.system.ISystemListener;

public class ClientForTest {

    private ISystemListener systemListener;

    public void setSystemListener(final ISystemListener systemListener) {
        this.systemListener = systemListener;
    }

    public void publishConnected() {
        systemListener.onConnect();
    }

    public void publishDisconnected() {
        systemListener.onDisconnect();
    }

    public void publishStrategyStarted(final long processId) {
        systemListener.onStart(processId);
    }

    public void publishStrategyStopped(final long processId) {
        systemListener.onStop(processId);
    }
}
