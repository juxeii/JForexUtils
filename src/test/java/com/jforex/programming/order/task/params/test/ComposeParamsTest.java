package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.functions.Action;

public class ComposeParamsTest extends CommonUtilForTest {

    private ComposeParams composeParams;

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
        composeParams = new ComposeParams();
    }

    @Test
    public void defaultValuesAreCorrect() throws Exception {
        composeParams.startAction().run();
        composeParams.completeAction().run();
        composeParams.errorConsumer().accept(jfException);

        final RetryParams retryParams = composeParams.retryParams();
        assertThat(retryParams.noOfRetries(), equalTo(0));
        assertThat(retryParams.delayFunction().apply(0).delay(), equalTo(0L));
    }

    @Test
    public void settersAreCorrect() throws Exception {
        composeParams.setStartAction(startActionMock);
        composeParams.setCompleteAction(completeActionMock);
        composeParams.setErrorConsumer(errorConsumerMock);
        composeParams.setRetryParams(retryParamsMock);

        assertThat(composeParams.startAction(), equalTo(startActionMock));
        assertThat(composeParams.completeAction(), equalTo(completeActionMock));
        assertThat(composeParams.errorConsumer(), equalTo(errorConsumerMock));
        assertThat(composeParams.retryParams(), equalTo(retryParamsMock));
    }
}
