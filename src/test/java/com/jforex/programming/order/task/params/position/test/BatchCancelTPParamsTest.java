package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.position.BatchCancelTPParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class BatchCancelTPParamsTest extends CommonParamsForTest {

    private BatchCancelTPParams batchCancelTPParams;

    @Mock
    public CancelTPParams cancelTPParamsMock;

    @Before
    public void setUp() {
        when(cancelTPParamsMock.consumerForEvent()).thenReturn(consumerForEvent);
    }

    @Test
    public void defaultValuesAreCorrect() {
        batchCancelTPParams = BatchCancelTPParams
            .newBuilder()
            .build();

        assertThat(batchCancelTPParams.batchMode(), equalTo(BatchMode.MERGE));
        assertNotNull(batchCancelTPParams.cancelTPParamsFactory());
    }

    @Test
    public void valuesAreCorrect() {
        final Function<IOrder, CancelTPParams> cancelTPFactory =
                order -> CancelTPParams.withOrder(order).build();

        batchCancelTPParams = BatchCancelTPParams
            .newBuilder()
            .withCancelTPParams(cancelTPFactory)
            .withBatchMode(BatchMode.CONCAT)
            .build();

        assertThat(batchCancelTPParams.cancelTPParamsFactory(), equalTo(cancelTPFactory));
        assertThat(batchCancelTPParams.batchMode(), equalTo(BatchMode.CONCAT));
        assertThat(batchCancelTPParams.consumerForEvent(), equalTo(consumerForEvent));
    }
}
