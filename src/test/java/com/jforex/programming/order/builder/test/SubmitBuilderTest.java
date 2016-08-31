package com.jforex.programming.order.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.builder.SubmitProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class SubmitBuilderTest extends CommonUtilForTest {

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final SubmitProcess submitBuilder = SubmitProcess
            .forOrderParams(buyParamsEURUSD)
            .build();

        assertNotNull(submitBuilder.errorAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> submitRejectAction = o -> {};
        final Consumer<IOrder> fillRejectAction = o -> {};
        final Consumer<IOrder> submitOKAction = o -> {};
        final Consumer<IOrder> partialFillAction = o -> {};
        final Consumer<IOrder> fillAction = o -> {};

        final SubmitProcess submitBuilder = SubmitProcess
            .forOrderParams(buyParamsEURUSD)
            .onError(errorAction)
            .onSubmitReject(submitRejectAction)
            .onFillReject(fillRejectAction)
            .onSubmitOK(submitOKAction)
            .onPartialFill(partialFillAction)
            .onFill(fillAction)
            .build();

        assertThat(submitBuilder.orderParams(), equalTo(buyParamsEURUSD));
        assertThat(submitBuilder.errorAction(), equalTo(errorAction));
    }
}
