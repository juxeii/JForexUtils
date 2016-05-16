package com.jforex.programming.position;

public final class RestoreSLTPData {

    private final double restoreSL;
    private final double restoreTP;

    public RestoreSLTPData(final double restoreSL,
                           final double restoreTP) {
        this.restoreSL = restoreSL;
        this.restoreTP = restoreTP;
    }

    public final double sl() {
        return restoreSL;
    }

    public final double tp() {
        return restoreTP;
    }
}
