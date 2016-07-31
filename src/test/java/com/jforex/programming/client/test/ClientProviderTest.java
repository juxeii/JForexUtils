package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.TesterFactory;
import com.jforex.programming.client.ClientProvider;
import com.jforex.programming.test.common.CommonUtilForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientFactory.class, TesterFactory.class })
@PowerMockIgnore({ "javax.management.*" })
public class ClientProviderTest extends CommonUtilForTest {

    @Before
    public void setUp() {
        mockStatic(ClientFactory.class);
        mockStatic(TesterFactory.class);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(ClientProvider.class);
    }

    @Test
    public void clientReturnsCorrectInstance() throws Exception {
        when(ClientFactory.getDefaultInstance()).thenReturn(clientMock);

        assertThat(ClientProvider.client(), equalTo(clientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenClientCreationFails() throws Exception {
        when(ClientFactory.getDefaultInstance())
                .thenThrow(new ClassNotFoundException());

        ClientProvider.client();
    }

    @Test
    public void testerClientReturnsCorrectInstance() throws Exception {
        when(TesterFactory.getDefaultInstance()).thenReturn(testerClientMock);

        assertThat(ClientProvider.testerClient(), equalTo(testerClientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenTesterClientCreationFails() throws Exception {
        when(TesterFactory.getDefaultInstance())
                .thenThrow(new ClassNotFoundException());

        ClientProvider.testerClient();
    }
}
