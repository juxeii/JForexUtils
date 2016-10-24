package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTask;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTaskTest extends InstrumentUtilForTest {

    private CancelSLTask cancelSLTask;

    @Mock
    private BatchChangeTask orderChangeBatchMock;
    @Mock
    private MergeParams mergeParamsMock;
    @Mock
    private OrderEventTransformer orderCancelSLComposerMock;
    private final Set<IOrder> toCancelSLOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        when(mergeParamsMock.orderCancelSLComposer(any())).thenReturn(orderCancelSLComposerMock);
        when(mergeParamsMock.orderCancelSLMode()).thenReturn(BatchMode.MERGE);

        cancelSLTask = new CancelSLTask(orderChangeBatchMock);
    }

    @Test
    public void observeIsDeferred() {
        cancelSLTask.observe(toCancelSLOrders, mergeParamsMock);

        verifyZeroInteractions(orderChangeBatchMock);
    }

    public class WhenSubscribedTests {

        private TestObserver<OrderEvent> testObserver;
        private final OrderEvent event = submitEvent;

        @Before
        public void setUp() {
            when(orderChangeBatchMock.cancelSL(eq(toCancelSLOrders),
                                               eq(BatchMode.MERGE),
                                               any()))
                                                   .thenReturn(eventObservable(event));

            testObserver = cancelSLTask
                .observe(toCancelSLOrders, mergeParamsMock)
                .test();
        }

        @Test
        public void subscribeCallsChangeBatchWithCorrecSLarams() {
            verify(orderChangeBatchMock)
                .cancelSL(eq(toCancelSLOrders),
                          eq(BatchMode.MERGE),
                          argThat(c -> {
                              Observable
                                  .fromCallable(() -> c.apply(buyOrderEURUSD))
                                  .test()
                                  .assertValue(orderCancelSLComposerMock);
                              return true;
                          }));
        }

        @Test
        public void observableFromChangeBatchIsReturned() {
            testObserver.assertValue(event);
            testObserver.assertComplete();
        }
    }
}
