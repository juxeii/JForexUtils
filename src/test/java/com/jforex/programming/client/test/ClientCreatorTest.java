package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.jforex.programming.client.ClientCreator;
import com.jforex.programming.test.common.CommonUtilForTest;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.TesterFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientFactory.class, TesterFactory.class })
public class ClientCreatorTest extends CommonUtilForTest {

    @Before
    public void setUp() {
        initCommonTestFramework();

        mockStatic(ClientFactory.class);
        mockStatic(TesterFactory.class);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(ClientCreator.class);
    }

    @Test
    public void clientReturnsCorrectInstance() throws Exception {
        when(ClientFactory.getDefaultInstance()).thenReturn(clientMock);

        assertThat(ClientCreator.client(), equalTo(clientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenClientCreationFails() throws Exception {
        when(ClientFactory.getDefaultInstance()).thenThrow(new ClassNotFoundException());

        ClientCreator.client();
    }

    @Test
    public void testerClientReturnsCorrectInstance() throws Exception {
        when(TesterFactory.getDefaultInstance()).thenReturn(testerClientMock);

        assertThat(ClientCreator.testerClient(), equalTo(testerClientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenTesterClientCreationFails() throws Exception {
        when(TesterFactory.getDefaultInstance()).thenThrow(new ClassNotFoundException());

        ClientCreator.testerClient();
    }
}
