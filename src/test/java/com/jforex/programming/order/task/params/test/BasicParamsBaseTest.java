package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.functions.Action;

public class BasicParamsBaseTest extends InstrumentUtilForTest {

    private CloseParams closeParams;

    @Mock
    public Action startActionMock;
    @Mock
    public Action completeActionMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;

    @Test
    public void basicParamsAreCorrect() {
        closeParams = CloseParams
            .closeOrder(buyOrderEURUSD)
            .doOnStart(startActionMock)
            .doOnComplete(completeActionMock)
            .doOnException(errorConsumerMock)
            .build();

        assertThat(closeParams.startAction(), equalTo(startActionMock));
        assertThat(closeParams.completeAction(), equalTo(completeActionMock));
        assertThat(closeParams.errorConsumer(), equalTo(errorConsumerMock));
    }
}
