package com.jforex.programming.position.test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionEvent;
import com.jforex.programming.position.PositionSubmitAndMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionSubmitAndMergeTest extends InstrumentUtilForTest {

    private PositionSubmitAndMerge positionSubmitAndMerge;

    @Mock private Position positionMock;
    private final Subject<PositionEvent, PositionEvent> positionEventSubject = PublishSubject.create();
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final String buyMergeLabel = userSettings.defaultMergePrefix() + orderParamsBUY.label();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        positionSubmitAndMerge = new PositionSubmitAndMerge(positionMock);
    }

    private void setUpMocks() {
        when(positionMock.positionEventObs()).thenReturn(positionEventSubject);
    }

    public class AfterSubmitAndMergeCall {

        @Before
        public void setUp() {
            positionSubmitAndMerge.submitAndMerge(orderParamsBUY, buyMergeLabel);
        }

        @Test
        public void testSubmitAndMergeCallSubmitOnPosition() {
            verify(positionMock).submit(orderParamsBUY);
        }

        public class AfterSubmitTaskDoneMessage {

            @Before
            public void setUp() {
                positionEventSubject.onNext(PositionEvent.SUBMITTASK_DONE);
            }

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(positionMock).merge(buyMergeLabel);
            }
        }

        public class AfterCloseTaskDoneMessage {

            @Before
            public void setUp() {
                positionEventSubject.onNext(PositionEvent.CLOSETASK_DONE);
            }

            @Test
            public void testNoMergeCallOnPosition() {
                verify(positionMock, never()).merge(buyMergeLabel);
            }
        }

        public class AfterMergeTaskDoneMessage {

            @Before
            public void setUp() {
                positionEventSubject.onNext(PositionEvent.MERGETASK_DONE);
            }

            @Test
            public void testNoMergeCallOnPosition() {
                verify(positionMock, never()).merge(buyMergeLabel);
            }
        }
    }
}
