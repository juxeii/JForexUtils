package com.jforex.programming.connection;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectionHandlerParams {

    private final int noOfLightReconnects;
    private final int noOfRelogins;
    private final long lightReconnectDelay;
    private final long reloginDelay;
    private final LoginCredentials loginCredentials;

    private ConnectionHandlerParams(final Builder builder) {
        noOfLightReconnects = builder.noOfLightReconnects;
        noOfRelogins = builder.noOfRelogins;
        lightReconnectDelay = builder.lightReconnectDelay;
        reloginDelay = builder.reloginDelay;
        loginCredentials = builder.loginCredentials;
    }

    public final int noOfLightReconnects() {
        return noOfLightReconnects;
    }

    public final int noOfRelogins() {
        return noOfRelogins;
    }

    public final long lightReconnectDelay() {
        return lightReconnectDelay;
    }

    public final long reloginDelay() {
        return reloginDelay;
    }

    public final LoginCredentials loginCredentials() {
        return loginCredentials;
    }

    public static Builder withLightReconnects(final int noOfLightReconnects,
                                              final long lightReconnectDelay) {
        return new Builder(noOfLightReconnects, lightReconnectDelay);
    }

    public static class Builder {

        private final int noOfLightReconnects;
        private int noOfRelogins;
        private final long lightReconnectDelay;
        private long reloginDelay;
        private LoginCredentials loginCredentials;

        public Builder(final int noOfLightReconnects,
                       final long lightReconnectDelay) {
            this.noOfLightReconnects = noOfLightReconnects;
            this.lightReconnectDelay = lightReconnectDelay;
        }

        public Builder withRelogins(final LoginCredentials loginCredentials,
                                    final int noOfRelogins,
                                    final long reloginDelay) {
            checkNotNull(loginCredentials);

            this.noOfRelogins = noOfRelogins;
            this.reloginDelay = reloginDelay;
            return this;
        }

        public ConnectionHandlerParams build() {
            return new ConnectionHandlerParams(this);
        }
    }
}
