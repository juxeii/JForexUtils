package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergePositionParamsMock)
            .build();

        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertNotNull(closePositionParams.mergePositionParams());
        assertNotNull(closePositionParams.closeParamsFactory().apply(buyOrderEURUSD));
    }

    @Test
    public void vluesAreCorrect() {
        final Function<IOrder, CloseParams> closeParamsFactory =
                order -> CloseParams.withOrder(order).build();

        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergePositionParamsMock)
            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .withCloseParams(closeParamsFactory)
            .build();

        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertThat(closePositionParams.closeParamsFactory(), equalTo(closeParamsFactory));
        assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));

        // TODO: add tests for consumerForEvents
    }
}
