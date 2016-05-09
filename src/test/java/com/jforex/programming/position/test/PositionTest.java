package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionTaskRejectException;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private ConcurrentUtil concurrentUtilMock;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final Subject<Long, Long> timerSubject = PublishSubject.create();
    private OrderParams orderParamsBuy;
    private OrderParams orderParamsSell;
    private Set<IOrder> toMergeOrders;
    private final String mergeLabel = "MergeLabel";
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        orderParamsBuy = OrderParamsForTest.paramsBuyEURUSD();
        orderParamsSell = OrderParamsForTest.paramsSellEURUSD();
        toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
        mergeOrder.setLabel(mergeLabel);
        setUpMocks();

        position = new Position(instrumentEURUSD,
                                orderUtilMock,
                                orderEventSubject,
                                restoreSLTPPolicyMock,
                                concurrentUtilMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);
        when(concurrentUtilMock.timerObservable(1500L, TimeUnit.MILLISECONDS)).thenReturn(timerSubject);
    }

    private Subject<OrderEvent, OrderEvent> setUpSubmit(final OrderParams orderParams) {
        return setUpObservable(() -> orderUtilMock.submit(orderParams));
    }

    private Subject<OrderEvent, OrderEvent> setUpObservable(final Supplier<Observable<OrderEvent>> orderEventSupplier) {
        final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
        when(orderEventSupplier.get()).thenReturn(orderEventSubject);
        return orderEventSubject;
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    @Test
    public void testCloseOnEmptyPositionDoesNotCallOnOrderUtil() {
        position.close();

        verifyZeroInteractions(orderUtilMock);
    }

    @Test
    public void testMergeOnEmptyPositionDoesNotCallOnOrderUtil() {
        position.merge(mergeLabel);

        verifyZeroInteractions(orderUtilMock);
    }

    public class AfterSubmit {

        protected Subject<OrderEvent, OrderEvent> buySubmitSubject;
        protected final TestSubscriber<OrderEvent> submitSubscriber = new TestSubscriber<>();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.CREATED);
            buySubmitSubject = setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy).subscribe(submitSubscriber);
        }

        @Test
        public void testSubmitIsCalledOnOrderUtil() {
            verify(orderUtilMock).submit(orderParamsBuy);
        }

        public class AfterFillRejectMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CANCELED);
                buySubmitSubject.onError(new PositionTaskRejectException("",
                                                                         new OrderEvent(buyOrder,
                                                                                        OrderEventType.FILL_REJECTED)));
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }

            @Test
            public void testNoRetryIsDone() {
                verify(orderUtilMock).submit(orderParamsBuy);
            }

            @Test
            public void testSubmitSubscriberIsNotifiedOfReject() {
                submitSubscriber.assertError(PositionTaskRejectException.class);
                submitSubscriber.assertValueCount(0);
            }
        }

        public class AfterFillMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.FILLED);
                buySubmitSubject.onNext(new OrderEvent(buyOrder, OrderEventType.FULL_FILL_OK));
            }

            @Test
            public void testPositionHasBuyOrder() {
                assertTrue(positionHasOrder(buyOrder));
            }

            @Test
            public void testSubmitSubscriberIsNotified() {
                submitSubscriber.assertNoErrors();
                submitSubscriber.assertValueCount(1);

                final OrderEvent orderEvent = submitSubscriber.getOnNextEvents().get(0);
                assertThat(orderEvent.order(), equalTo(buyOrder));
                assertThat(orderEvent.type(), equalTo(OrderEventType.FULL_FILL_OK));
            }
        }
    }
}
