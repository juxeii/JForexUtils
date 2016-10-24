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
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelTPTask;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelTPTaskTest extends InstrumentUtilForTest {

    private CancelTPTask cancelTPTask;

    private final Set<IOrder> toCancelTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Mock
    private BatchChangeTask orderChangeBatchMock;
    @Mock
    private MergeParams mergeParamsMock;
    @Mock
    private OrderEventTransformer orderCancelTPComposerMock;

    @Before
    public void setUp() {
        when(mergeParamsMock.orderCancelTPComposer(any())).thenReturn(orderCancelTPComposerMock);
        when(mergeParamsMock.orderCancelTPMode()).thenReturn(BatchMode.MERGE);

        cancelTPTask = new CancelTPTask(orderChangeBatchMock);
    }

    @Test
    public void observeIsDeferred() {
        cancelTPTask.observe(toCancelTPOrders, mergeParamsMock);

        verifyZeroInteractions(orderChangeBatchMock);
    }

    public class WhenSubscribedTests {

        private TestObserver<OrderEvent> testObserver;
        private final OrderEvent event = submitEvent;

        @Before
        public void setUp() {
            when(orderChangeBatchMock.cancelTP(eq(toCancelTPOrders),
                                               eq(BatchMode.MERGE),
                                               any()))
                                                   .thenReturn(eventObservable(event));

            testObserver = cancelTPTask
                .observe(toCancelTPOrders, mergeParamsMock)
                .test();
        }

        @Test
        public void subscribeCallsChangeBatchWithCorrectParams() {
            verify(orderChangeBatchMock)
                .cancelTP(eq(toCancelTPOrders),
                          eq(BatchMode.MERGE),
                          argThat(c -> {
                              Observable
                                  .fromCallable(() -> c.apply(buyOrderEURUSD))
                                  .test()
                                  .assertValue(orderCancelTPComposerMock);
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
