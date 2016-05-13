package com.jforex.programming.position.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionSubmitAndMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;

@RunWith(HierarchicalContextRunner.class)
public class PositionSubmitAndMergeTest extends InstrumentUtilForTest {

    private PositionSubmitAndMerge positionSubmitAndMerge;

    @Mock private Position positionMock;
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final String buyMergeLabel = userSettings.defaultMergePrefix() + orderParamsBUY.label();

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionSubmitAndMerge = new PositionSubmitAndMerge(positionMock);
    }

    public class AfterSubmitAndMergeCall {

        @Before
        public void setUp() {
            when(positionMock.submit(orderParamsBUY)).thenReturn(Completable.complete());

            positionSubmitAndMerge.submitAndMerge(orderParamsBUY, buyMergeLabel);
        }

        @Test
        public void testSubmitAndMergeCallSubmitOnPosition() {
            verify(positionMock).submit(orderParamsBUY);
        }

        public class AfterSubmitTaskDone {

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(positionMock).merge(buyMergeLabel);
            }
        }
    }
}
