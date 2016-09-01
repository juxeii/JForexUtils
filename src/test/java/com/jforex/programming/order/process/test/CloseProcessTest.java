package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
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
            .onCloseReject(closeRejectActionMock)
            .onClose(closeOKAction)
            .onPartialClose(partialCloseAction)
            .build();

        // verify(closeRejectActionMock).accept(buyOrderEURUSD);
    }
}
