package com.jforex.programming.order.test;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderCreateUtilTest extends InstrumentUtilForTest {

    private OrderCreateUtil orderCreateUtil;

    @Captor
    private ArgumentCaptor<Callable<IOrder>> orderCallCaptor;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderCreateUtil = new OrderCreateUtil(engineMock, orderUtilHandlerMock);
    }

    private void captureAndRunOrderCall(final OrderEventTypeData typeData) throws Exception {
        verify(orderUtilHandlerMock).submitObservable(orderCallCaptor.capture(),
                                                      eq(typeData));
        orderCallCaptor.getValue().call();
    }

    public class SubmitSetup {

        private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
        private final TestSubscriber<OrderEvent> submitSubscriber = new TestSubscriber<>();
        private final Observable<OrderEvent> submitObservable = Observable.empty();
        private final OrderEventTypeData submitTypeData = OrderEventTypeData.submitData;

        public class SubmitCall {

            @Before
            public void setUp() throws Exception {
                when(orderUtilHandlerMock
                        .submitObservable(orderCallCaptor.capture(), eq(submitTypeData)))
                                .thenReturn(submitObservable);

                orderCreateUtil.submitOrder(orderParamsBUY).subscribe(submitSubscriber);

                captureAndRunOrderCall(submitTypeData);
            }

            @Test
            public void testSubmitCallsOnIEngine() throws JFException {
                engineForTest.verifySubmit(orderParamsBUY, 1);
            }

            @Test
            public void testSuscriberIsNotifiesOnEvent() {
                orderEventSubject.onCompleted();

                submitSubscriber.assertCompleted();
            }

            @Test
            public void jfExceptionOnSubmitGetsPushed() {
                when(orderUtilHandlerMock
                        .submitObservable(orderCallCaptor.capture(), eq(submitTypeData)))
                                .thenReturn(Observable.error(jfException));

                orderCreateUtil.submitOrder(orderParamsBUY).subscribe(submitSubscriber);

                submitSubscriber.assertError(JFException.class);
            }
        }
    }

    public class MergeSetup {

        private final Set<IOrder> mergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                                IOrderForTest.sellOrderEURUSD());
        private final String mergeOrderLabel = "MergeLabel";
        private final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
        private final Observable<OrderEvent> mergeObservable = Observable.empty();
        private final OrderEventTypeData mergeTypeData = OrderEventTypeData.mergeData;

        public class MergeCall {

            @Before
            public void setUp() throws Exception {
                when(orderUtilHandlerMock
                        .submitObservable(orderCallCaptor.capture(), eq(mergeTypeData)))
                                .thenReturn(mergeObservable);

                orderCreateUtil.mergeOrders(mergeOrderLabel, mergeOrders)
                        .subscribe(mergeSubscriber);

                captureAndRunOrderCall(mergeTypeData);
            }

            @Test
            public void testMergeCallsOnIEngine() throws JFException {
                engineForTest.verifyMerge(mergeOrderLabel, mergeOrders, 1);
            }

            @Test
            public void testSuscriberIsNotifiesOnEvent() {
                orderEventSubject.onCompleted();

                mergeSubscriber.assertCompleted();
            }

            @Test
            public void jfExceptionOnMergeGetsPushed() {
                when(orderUtilHandlerMock
                        .submitObservable(orderCallCaptor.capture(), eq(mergeTypeData)))
                                .thenReturn(Observable.error(jfException));

                orderCreateUtil.mergeOrders(mergeOrderLabel, mergeOrders)
                        .subscribe(mergeSubscriber);

                mergeSubscriber.assertError(JFException.class);
            }
        }
    }
}
