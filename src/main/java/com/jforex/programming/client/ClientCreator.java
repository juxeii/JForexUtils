package com.jforex.programming.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.TesterFactory;

import rx.Observable;

public class ClientCreator {

    private interface IClientSupplier {

        public IClient get() throws ClassNotFoundException,
                             IllegalAccessException,
                             InstantiationException;
    }

    private final static Logger logger = LogManager.getLogger(ClientFactory.class);

    public final static IClient client() {
        return getInstance(ClientFactory::getDefaultInstance);
    }

    public final static ITesterClient testerClient() {
        return (ITesterClient) getInstance(TesterFactory::getDefaultInstance);
    }

    private final static IClient getInstance(final IClientSupplier clientSupplier) {
        return Observable.fromCallable(() -> clientSupplier.get())
                .doOnError(e -> {
                    logger.error("Exception occured on client retreival!" + e.getMessage());
                    System.exit(0);
                })
                .toBlocking()
                .first();
    }
}
