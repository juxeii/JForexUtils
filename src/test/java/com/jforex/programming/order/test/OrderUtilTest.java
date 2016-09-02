package com.jforex.programming.order.test;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

//@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

//    @Mock
//    private OrderUtilObservable orderUtilImplMock;
    @Mock
    private Position positionMock;
    @Mock
    private Consumer<IOrder> actionMock;
    @Mock
    private Callable<OrderEvent> orderEventCallableMock;

//    @Before
//    public void setUp() {
//        orderUtil = new OrderUtil(orderUtilImplMock);
//    }
//
//    private Observable<OrderEvent> observableForEvent(final OrderEventType type) {
//        final OrderEvent event = new OrderEvent(buyOrderEURUSD, type);
//        return Observable.just(event);
//    }
//
//    @Test
//    public void startSubmitDelegatesToOrderUtilImpl() {
//        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
//            .thenReturn(emptyObservable())
//            .thenReturn(jfExceptionObservable());
//
//        final OrderUtilCommand command = orderUtil
//            .submitBuilder(buyParamsEURUSD)
//            .doRetries(3, 1500L)
//            .build();
//
//        command.start();
//
//        verify(orderUtilImplMock).submitOrder(buyParamsEURUSD);
//    }
//
//    @Test
//    public void startSubmitCallsEventHandler() {
//        final Observable<OrderEvent> observable = observableForEvent(OrderEventType.SUBMIT_OK);
//
//        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
//            .thenReturn(observable);
//
//        final OrderUtilCommand command = orderUtil
//            .submitBuilder(buyParamsEURUSD)
//            .onSubmitOK(actionMock)
//            .build();
//
//        command.start();
//
//        verify(actionMock).accept(buyOrderEURUSD);
//    }
//
//    @Test
//    public void startSubmitCallsNotEventHandlerWhenNotSet() {
//        final Observable<OrderEvent> observable = observableForEvent(OrderEventType.SUBMIT_OK);
//
//        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
//            .thenReturn(observable);
//
//        final OrderUtilCommand command = orderUtil
//            .submitBuilder(buyParamsEURUSD)
//            .build();
//
//        command.start();
//
//        verify(actionMock, never()).accept(buyOrderEURUSD);
//    }
//
//    @Test
//    public void startSubmitRetriesOnReject() throws Exception {
//        final OrderEvent rejectEvent = new OrderEvent(buyOrderEURUSD,
//                                                      OrderEventType.SUBMIT_REJECTED);
//        final OrderEvent doneEvent = new OrderEvent(buyOrderEURUSD,
//                                                    OrderEventType.FULLY_FILLED);
//
//        when(orderEventCallableMock.call())
//            .thenReturn(rejectEvent)
//            .thenReturn(doneEvent);
//
//        final Observable<OrderEvent> observable = Observable.fromCallable(orderEventCallableMock);
//
//        when(orderUtilImplMock.submitOrder(buyParamsEURUSD))
//            .thenReturn(observable);
//
//        final OrderUtilCommand command = orderUtil
//            .submitBuilder(buyParamsEURUSD)
//            .onSubmitReject(actionMock)
//            .doRetries(1, 1500L)
//            .build();
//
//        command.start();
//
//        RxTestUtil.advanceTimeBy(1500L, TimeUnit.MILLISECONDS);
//
//        verify(actionMock).accept(buyOrderEURUSD);
//        verify(orderEventCallableMock, times(2)).call();
//    }
}
