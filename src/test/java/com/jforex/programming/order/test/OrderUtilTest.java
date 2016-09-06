package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    @Mock
    private IEngineUtil iengineUtilMock;
    @Mock
    private Action0 completeHandlerMock;
    @Mock
    private Action1<Throwable> errorHandlerMock;
    @Mock
    private Action0 startActionMock;

    @Before
    public void setUp() {
        setUpMocks();

        orderUtil = new OrderUtil(orderUtilHandlerMock,
                                  positionFactoryMock,
                                  iengineUtilMock);
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private void verifyOnCompleteIsCalled() {
        verify(startActionMock).call();
        verify(completeHandlerMock).call();
        verifyZeroInteractions(errorHandlerMock);
    }

    private void verifyOnErrorIsCalled() {
        verify(startActionMock).call();
        verify(errorHandlerMock).call(jfException);
        verifyZeroInteractions(completeHandlerMock);
    }

    public class SubmitOrderSetup {

        private SubmitCommand submitCommand;
        private final Callable<IOrder> callable = () -> buyOrderEURUSD;

        @Before
        public void setUp() {
            setUpMocks();

            submitCommand = orderUtil
                .submitBuilder(buyParamsEURUSD)
                .doOnStart(startActionMock)
                .build();
        }

        private void setUpMocks() {
            when(iengineUtilMock.submitCallable(buyParamsEURUSD))
                .thenReturn(callable);
        }

        @Test
        public void orderParamsIsSet() {
            assertThat(submitCommand.orderParams(), equalTo(buyParamsEURUSD));
        }

        @Test
        public void callableIsSet() {
            assertThat(submitCommand.callable(), equalTo(callable));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            submitCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(submitCommand))
                    .thenReturn(observable);

                submitCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
            }

            private Observable<OrderEvent> cretaeObservable(final OrderEventType type) {
                return eventObservable(buyOrderEURUSD, type);
            }

            private void verifyOrderIsAddedForEventType(final OrderEventType type) {
                subscribeForObservable(cretaeObservable(type));

                verify(positionMock).addOrder(buyOrderEURUSD);
            }

            private void verifyNoOrderIsAddedForEventType(final OrderEventType type) {
                subscribeForObservable(cretaeObservable(type));

                verify(positionMock, never()).addOrder(buyOrderEURUSD);
            }

            @Test
            public void onCompleteIsCalledWhenObservableCompletes() {
                subscribeForObservable(emptyObservable());

                verifyOnCompleteIsCalled();
            }

            @Test
            public void onErrorIsCalledWhenObservableEmitsAnError() {
                subscribeForObservable(jfExceptionObservable());

                verifyOnErrorIsCalled();
            }

            @Test
            public void onSubmitOKTheOrderIsAddedToPosition() {
                verifyOrderIsAddedForEventType(OrderEventType.SUBMIT_OK);
            }

            @Test
            public void onSubmitConditionalTheOrderIsAddedToPosition() {
                verifyOrderIsAddedForEventType(OrderEventType.SUBMIT_CONDITIONAL_OK);
            }

            @Test
            public void onSubmitRejectNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.SUBMIT_REJECTED);
            }

            @Test
            public void onFillRejectNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.FILL_REJECTED);
            }

            @Test
            public void onPartialFillNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.PARTIAL_FILL_OK);
            }

            @Test
            public void onFullyFillNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.FULLY_FILLED);
            }
        }
    }
}
