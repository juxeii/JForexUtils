package com.jforex.programming.order.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilCompletableTest extends InstrumentUtilForTest {

    private OrderUtilCompletable orderUtilCompletable;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    @Mock
    private Command commandMock;
    private TestSubscriber<Void> testSubscriber;
    private final Set<IOrder> ordersToMark = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtilCompletable = new OrderUtilCompletable(orderUtilHandlerMock, positionFactoryMock);
    }

    @Test
    public void toCompletableDefersCallToOrderUtilHandler() {
        orderUtilCompletable.forCommand(commandMock);

        verifyZeroInteractions(orderUtilHandlerMock);
    }

    @Test
    public void toCompletableDefersCallToOrderUtilHandlerWithOrderMarking() {
        orderUtilCompletable.forCommandWithOrderMarking(commandMock, ordersToMark);

        verifyZeroInteractions(orderUtilHandlerMock);
    }

    public class OnSubscribe {

        private void setUtilHandlerMockObservableAndSubscribe(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(commandMock)).thenReturn(observable);

            testSubscriber = orderUtilCompletable
                .forCommand(commandMock)
                .test();
        }

        @Test
        public void subscribeToCompletableInvokesOrderUtilHandler() {
            setUtilHandlerMockObservableAndSubscribe(emptyObservable());

            verify(orderUtilHandlerMock).callObservable(commandMock);
        }

        @Test
        public void toCompletableCompletesWhenOrderUtilHandlerCompletes() {
            setUtilHandlerMockObservableAndSubscribe(emptyObservable());

            testSubscriber.assertComplete();
        }

        @Test
        public void toCompletableNotYetCompletedWhenOrderUtilHandlerNotYetCompleted() {
            setUtilHandlerMockObservableAndSubscribe(neverObservable());

            testSubscriber.assertNotComplete();
        }

        @Test
        public void toCompletableErrorsWhenOrderUtilHandlerErrors() {
            setUtilHandlerMockObservableAndSubscribe(Observable.error(jfException));

            testSubscriber.assertNotComplete();
        }
    }

    public class OnSubscribeWithOrderMark {

        private void setUtilHandlerMockObservableWithOrderMarkAndSubscribe(final Observable<OrderEvent> observable) {
            when(orderUtilHandlerMock.callObservable(commandMock)).thenReturn(observable);

            testSubscriber = orderUtilCompletable
                .forCommandWithOrderMarking(commandMock, ordersToMark)
                .test();
        }

        @Before
        public void setUp() {
            positionMock = orderUtilForTest.createPositionMock(positionFactoryMock, instrumentEURUSD, ordersToMark);
        }

        @Test
        public void subscribeToCompletableInvokesOrderUtilHandler() {
            setUtilHandlerMockObservableWithOrderMarkAndSubscribe(emptyObservable());

            verify(orderUtilHandlerMock).callObservable(commandMock);
        }

        @Test
        public void toCompletableCompletesWhenOrderUtilHandlerCompletes() {
            setUtilHandlerMockObservableWithOrderMarkAndSubscribe(emptyObservable());

            testSubscriber.assertComplete();
        }

        @Test
        public void toCompletableNotYetCompletedWhenOrderUtilHandlerNotYetCompleted() {
            setUtilHandlerMockObservableWithOrderMarkAndSubscribe(neverObservable());

            testSubscriber.assertNotComplete();
        }

        @Test
        public void toCompletableErrorsWhenOrderUtilHandlerErrors() {
            setUtilHandlerMockObservableWithOrderMarkAndSubscribe(Observable.error(jfException));

            testSubscriber.assertNotComplete();
        }

        @Test
        public void ordersAreMarkedInCorrectSequence() {
            final InOrder inOrder = inOrder(positionMock, orderUtilHandlerMock);
            setUtilHandlerMockObservableWithOrderMarkAndSubscribe(emptyObservable());

            inOrder.verify(positionMock).markOrdersActive(ordersToMark);
            inOrder.verify(orderUtilHandlerMock).callObservable(commandMock);
            inOrder.verify(positionMock).markOrdersIdle(ordersToMark);
        }
    }
}
