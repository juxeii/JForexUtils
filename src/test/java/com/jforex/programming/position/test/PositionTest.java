package com.jforex.programming.position.test;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import com.dukascopy.api.JFException;

import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        position = new Position(instrumentEURUSD, orderEventSubject);
    }

    @Test
    public void testPositionHasBuyAndSellOrder() {
        ;
    }

//    public class CloseOnSL {
//
//        @Before
//        public void setUp() {
//            buyOrder.setState(IOrder.State.CLOSED);
//
//            sendOrderEvent(buyOrder, OrderEventType.CLOSED_BY_SL);
//        }
//
//        @Test
//        public void testPositionHasNoOrder() {
//            assertTrue(isRepositoryEmpty());
//        }
//    }
//
//    public class CloseOnTP {
//
//        @Before
//        public void setUp() {
//            buyOrder.setState(IOrder.State.CLOSED);
//
//            sendOrderEvent(buyOrder, OrderEventType.CLOSED_BY_TP);
//        }
//
//        @Test
//        public void testPositionHasNoOrder() {
//            assertTrue(isRepositoryEmpty());
//        }
//    }

}