package com.jforex.programming.order.task.params.position.test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.functions.Action;

public class PositionParamsBaseTest extends InstrumentUtilForTest {

    private CancelSLParams cancelSLParams;

    @Mock
    private Consumer<IOrder> startConsumerMock;
    @Mock
    private Consumer<IOrder> completeConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> errorConsumerMock;

    @Test
    public void handlersAreCorrect() throws Exception {
        cancelSLParams = CancelSLParams
            .newBuilder()
            .doOnStart(startConsumerMock)
            .doOnComplete(completeConsumerMock)
            .doOnError(errorConsumerMock)
            .build();

        final Action startAction = cancelSLParams.startAction(buyOrderEURUSD);
        final Action completeAction = cancelSLParams.completeAction(buyOrderEURUSD);
        final Consumer<Throwable> errorConsumer = cancelSLParams.errorConsumer(buyOrderEURUSD);

        startAction.run();
        completeAction.run();
        errorConsumer.accept(jfException);

        verify(startConsumerMock).accept(buyOrderEURUSD);
        verify(completeConsumerMock).accept(buyOrderEURUSD);
        verify(errorConsumerMock).accept(jfException, buyOrderEURUSD);
    }
}
