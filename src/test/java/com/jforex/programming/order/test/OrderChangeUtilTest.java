package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.RunnableWithJFException;
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
    private ArgumentCaptor<RunnableWithJFException> orderCallCaptor;
    @Captor
    private ArgumentCaptor<OrderEventTypeData> typeDataCaptor;
    private final IOrderForTest orderUnterTest = IOrderForTest.buyOrderEURUSD();
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final Observable<OrderEvent> changeObservable = Observable.empty();
    private final TestSubscriber<OrderEvent> changeSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderChangeUtil = new OrderChangeUtil(orderUtilHandlerMock);
    }

    private void captureAndRunOrderCall() throws JFException {
        verify(orderUtilHandlerMock).runOrderChangeCall(orderCallCaptor.capture(),
                                                        eq(orderUnterTest),
                                                        typeDataCaptor.capture());
        orderCallCaptor.getValue().run();
    }

    private void setUpHanderMock(final OrderEventTypeData orderEventTypeData) {
        when(orderUtilHandlerMock
                .runOrderChangeCall(orderCallCaptor.capture(), eq(orderUnterTest), eq(orderEventTypeData)))
                        .thenReturn(changeObservable);
    }

    private void runCommonTests(final OrderEventTypeData typeData) {
        assertTypeData(typeData);
        assertNotification();
    }

    private void assertTypeData(final OrderEventTypeData typeData) {
        final OrderEventTypeData orderEventTypeData = typeDataCaptor.getValue();

        assertThat(orderEventTypeData, equalTo(typeData));
    }

    private void assertNotification() {
        orderEventSubject.onCompleted();

        changeSubscriber.assertCompleted();
    }

    public class CloseCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.closeData;

        @Before
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.close(orderUnterTest).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).close();
        }
    }

    public class SetLabelCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeLabelData;
        private final String newLabel = "NewLabel";

        @Before
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setLabel(orderUnterTest, newLabel).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setLabel(newLabel);
        }
    }

    public class SetGTTCall {

        private final OrderEventTypeData typeData = OrderEventTypeData.changeGTTData;
        private final long newGTT = 1L;

        @Before
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setGoodTillTime(orderUnterTest, newGTT).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
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
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setOpenPrice(orderUnterTest, newOpenPrice).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
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
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setRequestedAmount(orderUnterTest, newRequestedAmount).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
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
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setStopLossPrice(orderUnterTest, newSL).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
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
        public void setUp() throws JFException {
            setUpHanderMock(typeData);

            orderChangeUtil.setTakeProfitPrice(orderUnterTest, newTP).subscribe(changeSubscriber);

            captureAndRunOrderCall();
            runCommonTests(typeData);
        }

        @Test
        public void testCallIsDoneOnOrder() {
            verify(orderUnterTest).setTakeProfitPrice(newTP);
        }
    }
}
