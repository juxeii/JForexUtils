package com.jforex.programming.order.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.builder.CloseBuilder;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CloseBuilderTest extends CommonUtilForTest {

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final CloseBuilder closeBuilder = CloseBuilder
                .forOrder(buyOrderEURUSD)
                .build();

        assertNotNull(closeBuilder.errorAction());
        assertNotNull(closeBuilder.closeRejectAction());
        assertNotNull(closeBuilder.closeOKAction());
        assertNotNull(closeBuilder.partialCloseAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> closeRejectAction = o -> {};
        final Consumer<IOrder> closeOKAction = o -> {};
        final Consumer<IOrder> partialCloseAction = o -> {};

        final CloseBuilder closeBuilder = CloseBuilder
                .forOrder(buyOrderEURUSD)
                .onError(errorAction)
                .onCloseReject(closeRejectAction)
                .onCloseOK(closeOKAction)
                .onPartialCloseOK(partialCloseAction)
                .build();

        assertThat(closeBuilder.orderToClose(), equalTo(buyOrderEURUSD));
        assertThat(closeBuilder.errorAction(), equalTo(errorAction));
        assertThat(closeBuilder.closeRejectAction(), equalTo(closeRejectAction));
        assertThat(closeBuilder.closeOKAction(), equalTo(closeOKAction));
        assertThat(closeBuilder.partialCloseAction(), equalTo(partialCloseAction));
    }
}
