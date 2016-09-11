package com.jforex.programming.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilCompletableTest extends InstrumentUtilForTest {

    private OrderUtilCompletable orderUtilCompletable;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilCompletable = new OrderUtilCompletable(orderUtilHandlerMock, positionFactoryMock);
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private void setUtilHandlerMockObservableForCommand(final CommonCommand command,
                                                        final Observable<OrderEvent> observable) {
        when(orderUtilHandlerMock.callObservable(command))
            .thenReturn(observable);
    }

    public class SubmitTests {

        private Completable submitCompletable;
        private final SubmitCommand submitCommandMock = mock(SubmitCommand.class);

        @Before
        public void setUp() {
            when(submitCommandMock.orderParams()).thenReturn(buyParamsEURUSD);

            submitCompletable = orderUtilCompletable.submitOrder(submitCommandMock);
        }

        private void setUtilHandlerMockObservable(final Observable<OrderEvent> observable) {
            setUtilHandlerMockObservableForCommand(submitCommandMock, observable);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(submitCommandMock);
            verifyZeroInteractions(orderUtilHandlerMock);
            verifyZeroInteractions(positionFactoryMock);
            verifyZeroInteractions(positionMock);
        }

        @Test
        public void onSubscribeOrderUtilHandlerIsCalled() {
            setUtilHandlerMockObservable(emptyObservable());

            submitCompletable.subscribe();

            verify(orderUtilHandlerMock).callObservable(submitCommandMock);
        }
    }
}
