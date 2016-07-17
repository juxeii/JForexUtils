package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

public class MergeCommandTest extends CommonUtilForTest {

    private OrderCallCommand command;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final IOrder mergeOrder = IOrderForTest.buyOrderEURUSD();
    private final Collection<IOrder> toMergeOrders =
            Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                            IOrderForTest.sellOrderEURUSD());

    @Before
    public void setUp() {
        initCommonTestFramework();

        command = new MergeCommand(mergeOrderLabel, toMergeOrders, engineMock);
        command.logOnSubscribe();
        command.logOnError(jfException);
        command.logOnCompleted();
    }

    @Test
    public void callableIsCorrect() throws Exception {
        when(engineMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(mergeOrder);

        final Callable<IOrder> callable = command.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(mergeOrder));
        verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertThat(command.orderEventTypeData(), equalTo(OrderEventTypeData.mergeData));
    }
}
