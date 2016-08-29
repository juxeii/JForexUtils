package com.jforex.programming.order.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.builder.SubmitBuilder;
import com.jforex.programming.test.common.CommonUtilForTest;

public class SubmitBuilderTest extends CommonUtilForTest {

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final SubmitBuilder submitBuilder = SubmitBuilder
                .forOrderParams(buyParamsEURUSD)
                .build();

        assertNotNull(submitBuilder.errorAction());
        assertNotNull(submitBuilder.submitRejectAction());
        assertNotNull(submitBuilder.fillRejectAction());
        assertNotNull(submitBuilder.submitOKAction());
        assertNotNull(submitBuilder.partialFillAction());
        assertNotNull(submitBuilder.fillAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> submitRejectAction = o -> {};
        final Consumer<IOrder> fillRejectAction = o -> {};
        final Consumer<IOrder> submitOKAction = o -> {};
        final Consumer<IOrder> partialFillAction = o -> {};
        final Consumer<IOrder> fillAction = o -> {};

        final SubmitBuilder submitBuilder = SubmitBuilder
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
        assertThat(submitBuilder.submitRejectAction(), equalTo(submitRejectAction));
        assertThat(submitBuilder.fillRejectAction(), equalTo(fillRejectAction));
        assertThat(submitBuilder.submitOKAction(), equalTo(submitOKAction));
        assertThat(submitBuilder.partialFillAction(), equalTo(partialFillAction));
        assertThat(submitBuilder.fillAction(), equalTo(fillAction));
    }
}
