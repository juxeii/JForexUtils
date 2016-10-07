package com.jforex.programming.order.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.OrderCancelTP;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderCancelTPTest extends InstrumentUtilForTest {

    private OrderCancelTP orderCancelTP;

    private final Set<IOrder> toCancelTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Mock
    private OrderChangeBatch orderChangeBatchMock;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private ObservableTransformer<OrderEvent, OrderEvent> orderCancelTPComposerMock;

    @Before
    public void setUp() {
        when(mergeCommandMock.orderCancelTPComposer(any())).thenReturn(orderCancelTPComposerMock);
        when(mergeCommandMock.orderCancelTPMode()).thenReturn(BatchMode.MERGE);

        orderCancelTP = new OrderCancelTP(orderChangeBatchMock);
    }

    @Test
    public void observeIsDeferred() {
        orderCancelTP.observe(toCancelTPOrders, mergeCommandMock);

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

            testObserver = orderCancelTP
                .observe(toCancelTPOrders, mergeCommandMock)
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
