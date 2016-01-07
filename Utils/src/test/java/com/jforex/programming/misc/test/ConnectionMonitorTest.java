package com.jforex.programming.misc.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IMessage;
import com.jforex.programming.misc.ConnectionMonitor;
import com.jforex.programming.test.fakes.IMessageForTest;

public class ConnectionMonitorTest {

    private ConnectionMonitor connectionMonitor;

    private final IMessage connectedMessage = IMessageForTest.connectedMessage();
    private final IMessage disconnectedMessage = IMessageForTest.disconnectedMessage();
    private final IMessage calendarMessage = IMessageForTest.calendarMessage();

    @Before
    public void setUp() {
        connectionMonitor = new ConnectionMonitor();
    }

    private void sendConnectedMessage() {
        connectionMonitor.onMessage(connectedMessage);
    }

    private void sendDisconnectedMessage() {
        connectionMonitor.onMessage(disconnectedMessage);
    }

    @Test
    public void testNotConnectionStateMessageIsIgnoredWhenConnected() {
        connectionMonitor.onMessage(calendarMessage);

        assertTrue(connectionMonitor.isConnected());
    }

    @Test
    public void testNotConnectionStateMessageIsIgnoredWhenDisconnected() {
        sendDisconnectedMessage();
        connectionMonitor.onMessage(calendarMessage);

        assertFalse(connectionMonitor.isConnected());
    }

    @Test
    public void testIsConnectedAfterServerSendsConnected() {
        sendConnectedMessage();

        assertTrue(connectionMonitor.isConnected());
    }

    @Test
    public void testIsDisconnectedAfterDisconnectMessage() {
        sendDisconnectedMessage();

        assertFalse(connectionMonitor.isConnected());
    }

    @Test
    public void testIsConnectedAfterDisconnectAndReconnect() {
        sendDisconnectedMessage();
        sendConnectedMessage();

        assertTrue(connectionMonitor.isConnected());
    }
}
