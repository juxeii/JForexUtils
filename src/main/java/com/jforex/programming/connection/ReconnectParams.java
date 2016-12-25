package com.jforex.programming.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.rx.RetryDelayFunction;

import io.reactivex.functions.Action;

public class ReconnectParams {

    private final Action reconnectStartAction;
    private final Action reconnectSuccessAction;
    private final Action reconnectFailAction;
    private final int noOfLightReconnects;
    private final RetryDelayFunction lightReconnectDelayFunction;
    private final LoginCredentials loginCredentials;
    private final int noOfRelogins;
    private final RetryDelayFunction reloginDelayFunction;

    private ReconnectParams(final Builder builder) {
        reconnectStartAction = builder.reconnectStartAction;
        reconnectSuccessAction = builder.reconnectSuccessAction;
        reconnectFailAction = builder.reconnectFailAction;
        noOfLightReconnects = builder.noOfLightReconnects;
        noOfRelogins = builder.noOfRelogins;
        lightReconnectDelayFunction = builder.lightReconnectDelayFunction;
        reloginDelayFunction = builder.reloginDelayFunction;
        loginCredentials = builder.loginCredentials;
    }

    public final Action reconnectStartAction() {
        return reconnectStartAction;
    }

    public final Action reconnectSuccessAction() {
        return reconnectSuccessAction;
    }

    public final Action reconnectFailAction() {
        return reconnectFailAction;
    }

    public final int noOfLightReconnects() {
        return noOfLightReconnects;
    }

    public final int noOfRelogins() {
        return noOfRelogins;
    }

    public final RetryDelayFunction lightReconnectDelayFunction() {
        return lightReconnectDelayFunction;
    }

    public final RetryDelayFunction reloginDelayFunction() {
        return reloginDelayFunction;
    }

    public final LoginCredentials loginCredentials() {
        return loginCredentials;
    }

    public static Builder withLightReconnects(final int noOfLightReconnects,
                                              final RetryDelayFunction lightReconnectDelayFunction) {
        return new Builder(noOfLightReconnects, lightReconnectDelayFunction);
    }

    public static class Builder {

        private Action reconnectStartAction = () -> {};
        private Action reconnectSuccessAction = () -> {};
        private Action reconnectFailAction = () -> {};
        private final int noOfLightReconnects;
        private int noOfRelogins;
        private final RetryDelayFunction lightReconnectDelayFunction;
        private RetryDelayFunction reloginDelayFunction;
        private LoginCredentials loginCredentials;

        public Builder(final int noOfLightReconnects,
                       final RetryDelayFunction lightReconnectDelayFunction) {
            this.noOfLightReconnects = noOfLightReconnects;
            this.lightReconnectDelayFunction = lightReconnectDelayFunction;
        }

        public Builder doOnReconnectStart(final Action reconnectStartAction) {
            checkNotNull(reconnectStartAction);

            this.reconnectStartAction = reconnectStartAction;
            return this;
        }

        public Builder doOnReconnectSuccess(final Action reconnectSuccessAction) {
            checkNotNull(reconnectSuccessAction);

            this.reconnectSuccessAction = reconnectSuccessAction;
            return this;
        }

        public Builder doOnReconnectFail(final Action reconnectFailAction) {
            checkNotNull(reconnectFailAction);

            this.reconnectFailAction = reconnectFailAction;
            return this;
        }

        public Builder withRelogins(final LoginCredentials loginCredentials,
                                    final int noOfRelogins,
                                    final RetryDelayFunction reloginDelayFunction) {
            checkNotNull(loginCredentials);

            this.loginCredentials = loginCredentials;
            this.noOfRelogins = noOfRelogins;
            this.reloginDelayFunction = reloginDelayFunction;
            return this;
        }

        public ReconnectParams build() {
            return new ReconnectParams(this);
        }
    }
}
