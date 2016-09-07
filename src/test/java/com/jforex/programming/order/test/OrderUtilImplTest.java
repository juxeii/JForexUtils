package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.OrderUtilImpl;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilImplTest extends InstrumentUtilForTest {

    private OrderUtilImpl orderUtilImpl;

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
    @Mock
    private Callable<IOrder> callableMock;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilImpl = new OrderUtilImpl(orderUtilHandlerMock,
                                          positionFactoryMock,
                                          iengineUtilMock);
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private Observable<OrderEvent> cretaeObservable(final OrderEventType type) {
        return eventObservable(buyOrderEURUSD, type);
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

    private CloseCommand closeCommandFactory(final IOrder order) {
        return orderUtilImpl
            .closeBuilder(order)
            .doOnStart(startActionMock)
            .build();
    }

    private MergeCommand mergeCommandFactory(final Set<IOrder> toMergeOrders,
                                             final String mergeOrderLabel) {
        return orderUtilImpl
            .mergeBuilder(mergeOrderLabel, toMergeOrders)
            .doOnStart(startActionMock)
            .build();
    }

    public class SubmitOrderSetup {

        private SubmitCommand submitCommand;

        @Before
        public void setUp() {
            setUpMocks();

            submitCommand = orderUtilImpl
                .submitBuilder(buyParamsEURUSD)
                .doOnStart(startActionMock)
                .build();
        }

        private void setUpMocks() {
            when(iengineUtilMock.submitCallable(buyParamsEURUSD))
                .thenReturn(callableMock);
        }

        @Test
        public void orderParamsIsSet() {
            assertThat(submitCommand.orderParams(), equalTo(buyParamsEURUSD));
        }

        @Test
        public void callableIsSet() {
            assertThat(submitCommand.callable(), equalTo(callableMock));
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

    public class MergeOrdersSetup {

        private MergeCommand mergeCommand;

        @Before
        public void setUp() {
            setUpMocks();

            mergeCommand = orderUtilImpl
                .mergeBuilder(mergeOrderLabel, toMergeOrders)
                .doOnStart(startActionMock)
                .build();
        }

        private void setUpMocks() {
            when(iengineUtilMock.mergeCallable(mergeOrderLabel, toMergeOrders))
                .thenReturn(callableMock);
        }

        @Test
        public void whenNotEnoughOrdersToMergeTheCommandCompletesEmpty() {
            mergeCommand = orderUtilImpl
                .mergeBuilder(mergeOrderLabel, Sets.newHashSet(buyOrderEURUSD))
                .doOnStart(startActionMock)
                .build();
            mergeCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void mergeOrderLabelIsSet() {
            assertThat(mergeCommand.mergeOrderLabel(), equalTo(mergeOrderLabel));
        }

        @Test
        public void toMergeOrdersIsSet() {
            assertThat(mergeCommand.toMergeOrders(), equalTo(toMergeOrders));
        }

        @Test
        public void callableIsSet() {
            assertThat(mergeCommand.callable(), equalTo(callableMock));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            mergeCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(mergeCommand))
                    .thenReturn(observable);

                mergeCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
            }

            private void verifyNoOrderIsAddedForEventType(final OrderEventType type) {
                subscribeForObservable(cretaeObservable(type));

                verify(positionMock, never()).addOrder(buyOrderEURUSD);
            }

            @Test
            public void ordersAreMarkedActiveAndIDLE() {
                subscribeForObservable(cretaeObservable(OrderEventType.MERGE_OK));

                final InOrder inOrder = inOrder(positionMock);

                inOrder.verify(positionMock).markOrdersActive(toMergeOrders);
                inOrder.verify(positionMock).markOrdersIdle(toMergeOrders);
            }

            @Test
            public void ordersAreMarkedActiveAndIdleForError() {
                subscribeForObservable(jfExceptionObservable());

                final InOrder inOrder = inOrder(positionMock);

                inOrder.verify(positionMock).markOrdersActive(toMergeOrders);
                inOrder.verify(positionMock).markOrdersIdle(toMergeOrders);
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
            public void onMergeTheOrderIsAddedToPosition() {
                subscribeForObservable(cretaeObservable(OrderEventType.MERGE_OK));

                verify(positionMock).addOrder(buyOrderEURUSD);
            }

            @Test
            public void onMergeCloseOKNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.MERGE_CLOSE_OK);
            }

            @Test
            public void onMergeRejectNoOrderIsAddedToPosition() {
                verifyNoOrderIsAddedForEventType(OrderEventType.MERGE_REJECTED);
            }
        }
    }

    public class CloseSetup {

        private CloseCommand closeCommand;

        @Before
        public void setUp() {
            closeCommand = orderUtilImpl
                .closeBuilder(buyOrderEURUSD)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(closeCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            closeCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenOrderIsAlreadyClosedTheCommandCompletesEmpty() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CLOSED);

            closeCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(closeCommand))
                    .thenReturn(observable);

                closeCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
            }

            @Test
            public void orderIsMarkedActiveAndIDLE() {
                subscribeForObservable(cretaeObservable(OrderEventType.CLOSE_OK));

                final InOrder inOrder = inOrder(positionMock);

                inOrder.verify(positionMock).markOrderActive(buyOrderEURUSD);
                inOrder.verify(positionMock).markOrderIdle(buyOrderEURUSD);
            }

            @Test
            public void ordersAreMarkedActiveAndIdleForError() {
                subscribeForObservable(jfExceptionObservable());

                final InOrder inOrder = inOrder(positionMock);

                inOrder.verify(positionMock).markOrderActive(buyOrderEURUSD);
                inOrder.verify(positionMock).markOrderIdle(buyOrderEURUSD);
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
        }
    }

    public class SetLabelSetup {

        private SetLabelCommand setLabelCommand;
        private static final String newLabel = "newLabel";

        @Before
        public void setUp() {
            setLabelCommand = orderUtilImpl
                .setLabelBuilder(buyOrderEURUSD, newLabel)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setLabelCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setLabelCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenLabelIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setLabel(buyOrderEURUSD, newLabel);

            setLabelCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setLabel(buyOrderEURUSD, "currentLabel");
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setLabelCommand))
                    .thenReturn(observable);

                setLabelCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class SetGTTSetup {

        private SetGTTCommand setGTTCommand;
        private static final long newGTT = 1L;

        @Before
        public void setUp() {
            setGTTCommand = orderUtilImpl
                .setGTTBuilder(buyOrderEURUSD, newGTT)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setGTTCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setGTTCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenGTTIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setGTT(buyOrderEURUSD, newGTT);

            setGTTCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setGTT(buyOrderEURUSD, 2L);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setGTTCommand))
                    .thenReturn(observable);

                setGTTCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class SetAmountSetup {

        private SetAmountCommand setAmountCommand;
        private static final double newAmount = 0.12;

        @Before
        public void setUp() {
            setAmountCommand = orderUtilImpl
                .setAmountBuilder(buyOrderEURUSD, newAmount)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setAmountCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setAmountCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenAmountIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setRequestedAmount(buyOrderEURUSD, newAmount);

            setAmountCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setRequestedAmount(buyOrderEURUSD, 0.2);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setAmountCommand))
                    .thenReturn(observable);

                setAmountCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class SetOpenPriceSetup {

        private SetOpenPriceCommand setOpenPriceCommand;
        private static final double newOpenPrice = 1.1234;

        @Before
        public void setUp() {
            setOpenPriceCommand = orderUtilImpl
                .setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setOpenPriceCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setOpenPriceCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenAmountIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setOpenPrice(buyOrderEURUSD, newOpenPrice);

            setOpenPriceCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setOpenPrice(buyOrderEURUSD, 1.4321);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setOpenPriceCommand))
                    .thenReturn(observable);

                setOpenPriceCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class SetSLSetup {

        private SetSLCommand setSLCommand;
        private static final double newSL = 1.1234;

        @Before
        public void setUp() {
            setSLCommand = orderUtilImpl
                .setSLBuilder(buyOrderEURUSD, newSL)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setSLCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setSLCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenAmountIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setSL(buyOrderEURUSD, newSL);

            setSLCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setSL(buyOrderEURUSD, 1.4321);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setSLCommand))
                    .thenReturn(observable);

                setSLCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class SetTPSetup {

        private SetTPCommand setTPCommand;
        private static final double newTP = 1.1234;

        @Before
        public void setUp() {
            setTPCommand = orderUtilImpl
                .setTPBuilder(buyOrderEURUSD, newTP)
                .doOnStart(startActionMock)
                .build();
        }

        @Test
        public void orderIsSet() {
            assertThat(setTPCommand.order(), equalTo(buyOrderEURUSD));
        }

        @Test
        public void gettingCompletableDoesNotCallOnOrderUtilHandler() {
            setTPCommand.completable();

            verifyZeroInteractions(orderUtilHandlerMock);
        }

        @Test
        public void whenAmountIsAlreadySetTheCommandCompletesEmpty() {
            orderUtilForTest.setTP(buyOrderEURUSD, newTP);

            setTPCommand
                .completable()
                .subscribe(completeHandlerMock, errorHandlerMock);

            verifyOnCompleteIsCalled();
            verifyZeroInteractions(orderUtilHandlerMock);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() {
                orderUtilForTest.setTP(buyOrderEURUSD, 1.4321);
            }

            private void subscribeForObservable(final Observable<OrderEvent> observable) {
                when(orderUtilHandlerMock.callObservable(setTPCommand))
                    .thenReturn(observable);

                setTPCommand
                    .completable()
                    .subscribe(completeHandlerMock, errorHandlerMock);
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
        }
    }

    public class ClosePositionSetup {

        private Completable closePosition;

        private void subscribeForObservable(final Set<IOrder> positionOrders) {
            when(orderUtilHandlerMock.callObservable(isA(CloseCommand.class)))
                .thenReturn(emptyObservable());
            when(positionMock.filledOrOpened()).thenReturn(positionOrders);

            closePosition = orderUtilImpl.closePosition(instrumentEURUSD, OrderUtilImplTest.this::closeCommandFactory);
            closePosition.subscribe(completeHandlerMock, errorHandlerMock);
        }

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.OPENED);
        }

        @Test
        public void onCompleteIsCalledWhenPositionHasNoOrders() {
            subscribeForObservable(Sets.newHashSet());

            verify(startActionMock, never()).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }

        @Test
        public void onCompleteIsCalledWhenAllCommandsComplete() {
            subscribeForObservable(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));

            verify(startActionMock, times(2)).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }
    }

    public class CloseAllPositionsSetup {

        private Completable closeAllPositions;

        private void subscribe() {
            closeAllPositions = orderUtilImpl.closeAllPositions(OrderUtilImplTest.this::closeCommandFactory);
            closeAllPositions.subscribe(completeHandlerMock, errorHandlerMock);
        }

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.OPENED);
        }

        @Test
        public void onCompleteIsCalledWhenNoPositionCreated() {
            when(positionFactoryMock.allPositions())
                .thenReturn(Sets.newHashSet());

            subscribe();

            verify(startActionMock, never()).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }

        @Test
        public void onCompleteIsCalledWhenAllPositionsAreClosed() {
            final Position positionMockOne = mock(Position.class);
            final Position positionMockTwo = mock(Position.class);

            when(positionFactoryMock.forInstrument(instrumentEURUSD))
                .thenReturn(positionMockOne);
            when(positionFactoryMock.forInstrument(instrumentAUDUSD))
                .thenReturn(positionMockTwo);

            when(positionMockOne.instrument())
                .thenReturn(instrumentEURUSD);
            when(positionMockTwo.instrument())
                .thenReturn(instrumentAUDUSD);
            when(positionMockOne.filledOrOpened())
                .thenReturn(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
            when(positionMockTwo.filledOrOpened())
                .thenReturn(Sets.newHashSet(sellOrderAUDUSD));
            when(positionFactoryMock.allPositions())
                .thenReturn(Sets.newHashSet(positionMockOne, positionMockTwo));

            subscribe();

            verify(startActionMock, times(3)).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }
    }

    public class MergePositionSetup {

        private Completable mergePosition;

        private void subscribeForObservable(final Set<IOrder> positionOrders) {
            when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
                .thenReturn(emptyObservable());
            when(positionMock.filled()).thenReturn(positionOrders);

            mergePosition = orderUtilImpl.mergePosition(instrumentEURUSD,
                                                        mergeOrderLabel,
                                                        OrderUtilImplTest.this::mergeCommandFactory);
            mergePosition.subscribe(completeHandlerMock, errorHandlerMock);
        }

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.FILLED);
        }

        @Test
        public void onCompleteIsCalledWhenPositionHasNoOrders() {
            subscribeForObservable(Sets.newHashSet());

            verify(startActionMock, never()).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }

        @Test
        public void onCompleteIsCalledMergeCompletes() {
            subscribeForObservable(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));

            verify(startActionMock).call();
            verify(completeHandlerMock).call();
            verifyZeroInteractions(errorHandlerMock);
        }
    }
}
