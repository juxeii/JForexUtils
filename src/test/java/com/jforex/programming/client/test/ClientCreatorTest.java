package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.TesterFactory;
import com.jforex.programming.client.ClientCreator;
import com.jforex.programming.test.common.CommonUtilForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientFactory.class, TesterFactory.class })
public class ClientCreatorTest extends CommonUtilForTest {

    @Before
    public void setUp() {
        initCommonTestFramework();

        PowerMockito.mockStatic(ClientFactory.class);
        PowerMockito.mockStatic(TesterFactory.class);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(ClientCreator.class);
    }

    @Test
    public void clientReturnsCorrectInstance() throws Exception {
        Mockito.when(ClientFactory.getDefaultInstance()).thenReturn(clientMock);

        assertThat(ClientCreator.client(), equalTo(clientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenClientCreationFails() throws Exception {
        Mockito.when(ClientFactory.getDefaultInstance()).thenThrow(ClassNotFoundException.class);

        ClientCreator.client();
    }

    @Test
    public void testerClientReturnsCorrectInstance() throws Exception {
        Mockito.when(TesterFactory.getDefaultInstance()).thenReturn(testerClientMock);

        assertThat(ClientCreator.testerClient(), equalTo(testerClientMock));
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownWhenTesterClientCreationFails() throws Exception {
        Mockito.when(TesterFactory.getDefaultInstance()).thenThrow(ClassNotFoundException.class);

        ClientCreator.testerClient();
    }
}
