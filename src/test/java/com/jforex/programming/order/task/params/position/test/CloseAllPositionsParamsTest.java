package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CloseAllPositionsParamsTest extends CommonParamsForTest {

    private CloseAllPositionsParams closeAllPositionsParams;

    @Mock
    private Function<Instrument, ClosePositionParams> closePositonParamsFactoryMock;

    @Before
    public void setUp() {
        closeAllPositionsParams = CloseAllPositionsParams
            .newBuilder(closePositonParamsFactoryMock)
            .doOnStart(actionMock)
            .doOnComplete(actionMock)
            .doOnError(errorConsumerMock)
            .retryOnReject(retryParams)
            .build();
    }

    @Test
    public void createClosePositionParamsCreatesCorrectInstance() {
        assertThat(closeAllPositionsParams.closePositonParamsFactory(),
                   equalTo(closePositonParamsFactoryMock));
    }

    @Test
    public void noConsumerForEventsArePresent() {
        assertTrue(closeAllPositionsParams
            .composeData()
            .consumerByEventType()
            .isEmpty());
    }

    @Test
    public void assertComposeDataAreCorrect() {
        assertComposeData(closeAllPositionsParams.composeData());
    }

    @Test
    public void typeIsCLOSEALLPOSITIONS() {
        assertThat(closeAllPositionsParams.type(), equalTo(TaskParamsType.CLOSEALLPOSITIONS));
    }
}
