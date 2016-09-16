package com.jforex.programming.order.test;

import org.junit.Before;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderTaskData;
import com.jforex.programming.order.OrderTaskDataFactory;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

//@RunWith(HierarchicalContextRunner.class)
public class OrderUtilHandlerTest extends InstrumentUtilForTest {

    private OrderUtilHandler orderUtilHandler;

    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Mock
    private OrderTaskDataFactory orderTaskDataFactoryMock;
    @Mock
    private OrderTaskData orderTaskDataMock;
    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(orderEventGatewayMock, orderTaskDataFactoryMock);
    }

    public void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
    }

    private OrderEvent sendOrderEvent(final IOrder order,
                                      final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     orderEventType,
                                                     true);
        orderEventSubject.onNext(orderEvent);
        return orderEvent;
    }
}
