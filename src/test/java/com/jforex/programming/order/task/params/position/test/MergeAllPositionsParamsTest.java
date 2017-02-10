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
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergeAllPositionsParamsTest extends CommonParamsForTest {

    private MergeAllPositionsParams mergeAllPositionsParams;

    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private Function<Instrument, MergePositionParams> mergeParamsFactoryMock;

    @Before
    public void setUp() {
        when(mergeParamsFactoryMock.apply(instrumentEURUSD)).thenReturn(mergePositionParamsMock);

        mergeAllPositionsParams = MergeAllPositionsParams
            .withMergeParamsFactory(mergeParamsFactoryMock)
            .doOnStart(actionMock)
            .doOnComplete(actionMock)
            .doOnError(errorConsumerMock)
            .retryOnReject(retryParams)
            .build();
    }

    @Test
    public void typeIsMERGEALLPOSITIONS() {
        assertThat(mergeAllPositionsParams.type(), equalTo(TaskParamsType.MERGEALLPOSITIONS));
    }

    @Test
    public void createMergePositionParamsCreatesCorrectInstance() {
        assertThat(mergeAllPositionsParams.createMergePositionParams(instrumentEURUSD),
                   equalTo(mergePositionParamsMock));
    }

    @Test
    public void noConsumerForEventsArePresent() {
        assertTrue(mergeAllPositionsParams.consumerForEvent().isEmpty());
    }

    @Test
    public void assertComposeDataAreCorrect() {
        assertComposeData(mergeAllPositionsParams.composeData());
    }
}
