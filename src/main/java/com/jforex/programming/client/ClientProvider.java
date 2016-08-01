package com.jforex.programming.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.TesterFactory;

import rx.Observable;

public final class ClientProvider {

    private ClientProvider() {
    }

    private interface IClientSupplier {
        public IClient get() throws ClassNotFoundException,
                             IllegalAccessException,
                             InstantiationException;
    }

    private static final Logger logger = LogManager.getLogger(ClientFactory.class);

    public static final IClient client() {
        return get(ClientFactory::getDefaultInstance);
    }

    public static final ITesterClient testerClient() {
        return (ITesterClient) get(TesterFactory::getDefaultInstance);
    }

    private static final IClient get(final IClientSupplier clientSupplier) {
        return Observable
                .fromCallable(clientSupplier::get)
                .doOnError(e -> logger.error("IClient retreival failed! " + e.getMessage()))
                .toBlocking()
                .first();
    }
}
