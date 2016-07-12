package com.jforex.programming.client.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ITesterClient;
import com.jforex.programming.client.ClientCreator;

public class ClientCreatorTest {

    @Test
    public void clientReturnsCorrectInstance() {
        assertThat(ClientCreator.client(), instanceOf(IClient.class));
    }

    @Test
    public void testerClientReturnsCorrectInstance() {
        assertThat(ClientCreator.testerClient(), instanceOf(ITesterClient.class));
    }
}
