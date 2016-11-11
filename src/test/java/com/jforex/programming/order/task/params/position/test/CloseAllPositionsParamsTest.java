package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CloseAllPositionsParamsTest extends CommonParamsForTest {

    private CloseAllPositionsParams closeAllPositionsParams;

    @Mock
    public ClosePositionParams closePositionParamsMock;

    @Test
    public void valuesAreCorrect() {
        closeAllPositionsParams = CloseAllPositionsParams
            .newBuilder(instrument -> closePositionParamsMock)
            .build();

        assertThat(closeAllPositionsParams.paramsFactory().apply(instrumentEURUSD),
                   equalTo(closePositionParamsMock));
    }
}
