package com.jforex.programming.misc;

import static com.jforex.programming.misc.JForexUtil.pfs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;

public final class ConnectionMonitor implements MessageConsumer {

    private boolean isConnected = true;

    private final static Logger logger = LogManager.getLogger(ConnectionMonitor.class);

    public final boolean isConnected() {
        return isConnected;
    }

    @Override
    public final void onMessage(final IMessage connectionStatusMessage) {
        processConnectionMessageContent(connectionStatusMessage.getContent());
    }

    private final void processConnectionMessageContent(final String messageContent) {
        if (messageContent.equals(pfs.DISCONNECTED_STRING())) {
            logger.warn("Disconnected message received!");
            isConnected = false;
        } else if (messageContent.equals(pfs.CONNECTED_STRING())) {
            logger.info("Connected message received.");
            isConnected = true;
        }
    }
}
