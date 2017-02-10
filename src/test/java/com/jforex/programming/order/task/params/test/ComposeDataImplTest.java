package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.functions.Action;

public class ComposeDataImplTest extends CommonUtilForTest {

    private ComposeDataImpl composeDataImpl;

    @Mock
    private Action startActionMock;
    @Mock
    private Action completeActionMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private RetryParams retryParamsMock;

    @Before
    public void setUp() {
        composeDataImpl = new ComposeDataImpl();
    }

    @Test
    public void defaultValuesAreCorrect() throws Exception {
        composeDataImpl.startAction().run();
        composeDataImpl.completeAction().run();
        composeDataImpl.errorConsumer().accept(jfException);

        final RetryParams retryParams = composeDataImpl.retryParams();
        assertThat(retryParams.noOfRetries(), equalTo(0));
        assertThat(retryParams.delayFunction().apply(0).delay(), equalTo(0L));
    }

    @Test
    public void settersAreCorrect() throws Exception {
        composeDataImpl.setStartAction(startActionMock);
        composeDataImpl.setCompleteAction(completeActionMock);
        composeDataImpl.setErrorConsumer(errorConsumerMock);
        composeDataImpl.setRetryParams(retryParamsMock);

        assertThat(composeDataImpl.startAction(), equalTo(startActionMock));
        assertThat(composeDataImpl.completeAction(), equalTo(completeActionMock));
        assertThat(composeDataImpl.errorConsumer(), equalTo(errorConsumerMock));
        assertThat(composeDataImpl.retryParams(), equalTo(retryParamsMock));
    }
}
