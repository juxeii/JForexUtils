package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.CloseProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CloseProcessTest extends CommonUtilForTest {

    @Mock
    public Consumer<IOrder> closeRejectActionMock;

    @Test
    public void orderToCloseIsCorrectt() {
        final CloseProcess process = CloseProcess
            .forOrder(buyOrderEURUSD)
            .build();

        assertThat(process.orderToClose(), equalTo(buyOrderEURUSD));
    }

    @Test
    public void actionsAreCorrectMapped() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> closeOKAction = o -> {};
        final Consumer<IOrder> partialCloseAction = o -> {};

        final CloseProcess process = CloseProcess
            .forOrder(buyOrderEURUSD)
            .onError(errorAction)
            .onClose(closeOKAction)
            .onCloseReject(closeRejectActionMock)
            .onPartialClose(partialCloseAction)
            .build();

        assertThat(process.eventHandlerForType().size(), equalTo(3));
        assertTrue(process.eventHandlerForType().containsKey(OrderEventType.CLOSE_REJECTED));

        process.eventHandlerForType().get(OrderEventType.CLOSE_REJECTED).accept(buyOrderEURUSD);
        verify(closeRejectActionMock).accept(buyOrderEURUSD);
    }
}
