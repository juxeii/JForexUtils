package com.jforex.programming.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.JFException;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderChangeUtilTest extends InstrumentUtilForTest {

    private OrderChangeUtil orderChangeUtil;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Captor
    private ArgumentCaptor<JFRunnable> orderCallCaptor;
    private final IOrderForTest orderUnterTest = IOrderForTest.buyOrderEURUSD();
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final Observable<OrderEvent> changeObservable = Observable.empty();
    private final TestSubscriber<OrderEvent> changeSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderChangeUtil = new OrderChangeUtil(orderUtilHandlerMock);
    }

    private void captureAndRunOrderCall(final OrderEventTypeData typeData) throws Exception {
        verify(orderUtilHandlerMock).changeObservable(orderCallCaptor.capture(),
                                                      eq(orderUnterTest),
                                                      eq(typeData));
        orderCallCaptor.getValue().run();
    }

    private void setUpHanderMock(final OrderEventTypeData orderEventTypeData) {
        when(orderUtilHandlerMock
                .changeObservable(orderCallCaptor.capture(), eq(orderUnterTest),
                                  eq(orderEventTypeData)))
                                          .thenReturn(changeObservable);
    }

    private void assertNotification() {
        orderEventSubject.onCompleted();

        changeSubscriber.assertCompleted();
    }

    public class CloseCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.closeData;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.close(orderUnterTest).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).close();
        }

        @Test
        public void jfExceptionOnCloseGetsPushed() {
            when(orderUtilHandlerMock
                    .changeObservable(any(),
                                      eq(orderUnterTest),
                                      eq(typeData)))
                                              .thenReturn(Observable.error(jfException));

            orderChangeUtil.close(orderUnterTest).subscribe(changeSubscriber);

            changeSubscriber.assertError(JFException.class);
        }
    }

    public class SetLabelCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeLabelData;
        private final String newLabel = "NewLabel";

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setLabel(orderUnterTest, newLabel).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setLabel(newLabel);
        }

        @Test
        public void jfExceptionOnSetLabelGetsPushed() {
            when(orderUtilHandlerMock
                    .changeObservable(any(),
                                      eq(orderUnterTest),
                                      eq(typeData)))
                                              .thenReturn(Observable.error(jfException));

            orderChangeUtil.setLabel(orderUnterTest, newLabel).subscribe(changeSubscriber);

            changeSubscriber.assertError(JFException.class);
        }
    }

    public class SetGTTCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeGTTData;
        private final long newGTT = 1L;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setGoodTillTime(orderUnterTest, newGTT).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setGoodTillTime(newGTT);
        }
    }

    public class SetOpenPriceCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeOpenPriceData;
        private final double newOpenPrice = askEURUSD;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setOpenPrice(orderUnterTest, newOpenPrice).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setOpenPrice(newOpenPrice);
        }
    }

    public class SetRequestedAmountCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeAmountData;
        private final double newRequestedAmount = 0.12;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setRequestedAmount(orderUnterTest, newRequestedAmount)
                    .subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setRequestedAmount(newRequestedAmount);
        }
    }

    public class SetSLCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeSLData;
        private final double newSL = 1.09123;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setStopLossPrice(orderUnterTest, newSL).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setStopLossPrice(newSL);
        }
    }

    public class SetTPCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeTPData;
        private final double newTP = 1.09123;

        @Before
        public void setUp() throws Exception {
            setUpHanderMock(typeData);

            orderChangeUtil.setTakeProfitPrice(orderUnterTest, newTP).subscribe(changeSubscriber);

            captureAndRunOrderCall(typeData);
            assertNotification();
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setTakeProfitPrice(newTP);
        }
    }
}
