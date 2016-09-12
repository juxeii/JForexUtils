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
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.CommonCommand;
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
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Action;

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
    private Action completedActionMock;
    private Completable callCompletable;

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilCompletable = spy(new OrderUtilCompletable(orderUtilHandlerMock, positionFactoryMock));
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(positionMock);
    }

    private void setUtilHandlerMockObservableForCommand(final CommonCommand command,
                                                        final Observable<OrderEvent> observable) {
        when(orderUtilHandlerMock.callObservable(command)).thenReturn(observable);
    }

    private void verifyNoInteractionsToMocks() {
        verifyZeroInteractions(orderUtilHandlerMock);
        verifyZeroInteractions(positionFactoryMock);
        verifyZeroInteractions(positionMock);
    }

    private void setUpCommandToCompletable(final CommonCommand command,
                                           final OrderCallReason callReason) {
        when(command.callReason()).thenReturn(callReason);
        orderUtilCompletable
            .commandToCompletable(command)
            .subscribe();
    }

    public class SubmitTests {

        private final SubmitCommand submitCommandMock = mock(SubmitCommand.class);

        @Before
        public void setUp() {
            callCompletable = orderUtilCompletable.submitOrder(submitCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(submitCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void onSubscribeOrderUtilHandlerIsCalled() throws Exception {
            setUtilHandlerMockObservableForCommand(submitCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(submitCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToSubmitCall() {
            setUtilHandlerMockObservableForCommand(submitCommandMock, emptyObservable());
            setUpCommandToCompletable(submitCommandMock, OrderCallReason.SUBMIT);

            verify(orderUtilCompletable).submitOrder(submitCommandMock);
        }
    }

    public class MergeTests {

        private final Set<IOrder> toMergeOrders = Sets.newHashSet();
        private final MergeCommand mergeCommandMock = mock(MergeCommand.class);

        @Before
        public void setUp() {
            when(mergeCommandMock.toMergeOrders()).thenReturn(toMergeOrders);

            callCompletable = orderUtilCompletable.mergeOrders(mergeCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(mergeCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void commandToCompletableMapsToMergeCall() {
            setUtilHandlerMockObservableForCommand(mergeCommandMock, emptyObservable());
            setUpCommandToCompletable(mergeCommandMock, OrderCallReason.MERGE);

            verify(orderUtilCompletable).mergeOrders(mergeCommandMock);
        }

        public class NoOrdersToMerge {

            @Before
            public void setUp() {
                callCompletable.subscribe(completedActionMock);
            }

            @Test
            public void callCompletesImmediately() throws Exception {
                verify(completedActionMock).run();
            }

            @Test
            public void noOrderUtilHandlerInteraction() {
                verifyZeroInteractions(orderUtilHandlerMock);
            }

            @Test
            public void noPositionFactoryInteraction() {
                verifyZeroInteractions(positionFactoryMock);
            }
        }

        public class OneOrderForMerge {

            @Before
            public void setUp() {
                toMergeOrders.add(buyOrderEURUSD);

                callCompletable.subscribe(completedActionMock);
            }

            @Test
            public void callCompletesImmediately() throws Exception {
                verify(completedActionMock).run();
            }

            @Test
            public void noOrderUtilHandlerInteraction() {
                verifyZeroInteractions(orderUtilHandlerMock);
            }

            @Test
            public void noPositionFactoryInteraction() {
                verifyZeroInteractions(positionFactoryMock);
            }
        }

        public class TwoOrdersForMerge {

            private void setUtilHandlerMockObservableAndSubscribe(final Observable<OrderEvent> observable) {
                setUtilHandlerMockObservableForCommand(mergeCommandMock, observable);

                callCompletable.subscribe(completedActionMock);
            }

            @Before
            public void setUp() {
                toMergeOrders.add(buyOrderEURUSD);
                toMergeOrders.add(sellOrderEURUSD);
            }

            @Test
            public void orderUtilHandlerCallIsEmbeddedInMarkingOrdersActiveAndIdle() throws Exception {
                setUtilHandlerMockObservableAndSubscribe(emptyObservable());

                final InOrder inOrder = inOrder(positionMock, orderUtilHandlerMock);

                inOrder.verify(positionMock).markOrdersActive(toMergeOrders);
                inOrder.verify(orderUtilHandlerMock).callObservable(mergeCommandMock);
                inOrder.verify(positionMock).markOrdersIdle(toMergeOrders);
                verify(completedActionMock).run();
            }

            @Test
            public void ordersAreMarkedIdleOnlyWhenOrderUtilHandlerTerminated() {
                setUtilHandlerMockObservableAndSubscribe(neverObservable());

                verify(positionMock, never()).markOrdersIdle(toMergeOrders);
                verifyZeroInteractions(completedActionMock);
            }
        }
    }

    public class CloseTests {

        private final IOrder orderToClose = buyOrderEURUSD;
        private final OrderEvent closeEvent = new OrderEvent(orderToClose,
                                                             OrderEventType.CLOSE_OK,
                                                             true);
        private final CloseCommand closeCommandMock = mock(CloseCommand.class);

        @Before
        public void setUp() {
            when(closeCommandMock.order()).thenReturn(orderToClose);

            callCompletable = orderUtilCompletable.close(closeCommandMock);
        }

        private void setUtilHandlerMockObservableAndSubscribe(final Observable<OrderEvent> observable) {
            setUtilHandlerMockObservableForCommand(closeCommandMock, observable);

            callCompletable.subscribe(completedActionMock);
        }

        private void verifyOrderUtilHandlerCallIsEmbeddedInMarkingOrderActiveAndIdle() throws Exception {
            setUtilHandlerMockObservableAndSubscribe(eventObservable(closeEvent));

            final InOrder inOrder = inOrder(positionMock, orderUtilHandlerMock);

            inOrder.verify(positionMock).markOrderActive(orderToClose);
            inOrder.verify(orderUtilHandlerMock).callObservable(closeCommandMock);
            inOrder.verify(positionMock).markOrderIdle(orderToClose);
            verify(completedActionMock).run();
        }

        private void verifyOrderIsMarkedIdleOnlyWhenOrderUtilHandlerTerminated() {
            setUtilHandlerMockObservableAndSubscribe(neverObservable());

            verify(positionMock, never()).markOrderIdle(orderToClose);
            verifyZeroInteractions(completedActionMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(closeCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenOrderAlreadyClosedTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setState(orderToClose, IOrder.State.CLOSED);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(closeCommandMock);
            verify(positionMock, never()).markOrderActive(orderToClose);
            verify(positionMock, never()).markOrderIdle(orderToClose);
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(closeCommandMock, emptyObservable());
            setUpCommandToCompletable(closeCommandMock, OrderCallReason.CLOSE);

            verify(orderUtilCompletable).close(closeCommandMock);
        }

        public class OrderIsFilled {

            @Before
            public void setUp() {
                orderUtilForTest.setState(orderToClose, IOrder.State.FILLED);
            }

            @Test
            public void orderUtilHandlerCallIsEmbeddedInMarkingOrderActiveAndIdle() throws Exception {
                verifyOrderUtilHandlerCallIsEmbeddedInMarkingOrderActiveAndIdle();
            }

            @Test
            public void orderIsMarkedIdleOnlyWhenOrderUtilHandlerTerminated() {
                verifyOrderIsMarkedIdleOnlyWhenOrderUtilHandlerTerminated();
            }
        }

        public class OrderIsOpened {

            @Before
            public void setUp() {
                orderUtilForTest.setState(orderToClose, IOrder.State.OPENED);
            }

            @Test
            public void orderUtilHandlerCallIsEmbeddedInMarkingOrderActiveAndIdle() throws Exception {
                verifyOrderUtilHandlerCallIsEmbeddedInMarkingOrderActiveAndIdle();
            }

            @Test
            public void orderIsMarkedIdleOnlyWhenOrderUtilHandlerTerminated() {
                verifyOrderIsMarkedIdleOnlyWhenOrderUtilHandlerTerminated();
            }
        }
    }

    public class SetLabelTests {

        private final IOrder orderToSetLabel = buyOrderEURUSD;
        private final String newLabel = "newLabel";
        private final SetLabelCommand setLabelCommandMock = mock(SetLabelCommand.class);

        @Before
        public void setUp() {
            when(setLabelCommandMock.order()).thenReturn(orderToSetLabel);
            when(setLabelCommandMock.newLabel()).thenReturn(newLabel);

            callCompletable = orderUtilCompletable.setLabel(setLabelCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setLabelCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewLabelIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setLabel(orderToSetLabel, newLabel);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setLabelCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewLabel() throws Exception {
            setUtilHandlerMockObservableForCommand(setLabelCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setLabelCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setLabelCommandMock, emptyObservable());
            setUpCommandToCompletable(setLabelCommandMock, OrderCallReason.CHANGE_LABEL);

            verify(orderUtilCompletable).setLabel(setLabelCommandMock);
        }
    }

    public class SetGTTTests {

        private final IOrder orderToSetGTT = buyOrderEURUSD;
        private final long newGTT = 1L;
        private final SetGTTCommand setGTTCommandMock = mock(SetGTTCommand.class);

        @Before
        public void setUp() {
            when(setGTTCommandMock.order()).thenReturn(orderToSetGTT);
            when(setGTTCommandMock.newGTT()).thenReturn(newGTT);

            callCompletable = orderUtilCompletable.setGTT(setGTTCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setGTTCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewGTTIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setGTT(orderToSetGTT, newGTT);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setGTTCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewGTT() throws Exception {
            setUtilHandlerMockObservableForCommand(setGTTCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setGTTCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setGTTCommandMock, emptyObservable());
            setUpCommandToCompletable(setGTTCommandMock, OrderCallReason.CHANGE_GTT);

            verify(orderUtilCompletable).setGTT(setGTTCommandMock);
        }
    }

    public class SetAmountTests {

        private final IOrder orderToSetAmount = buyOrderEURUSD;
        private final double newAmount = 0.24;
        private final SetAmountCommand setAmountCommandMock = mock(SetAmountCommand.class);

        @Before
        public void setUp() {
            when(setAmountCommandMock.order()).thenReturn(orderToSetAmount);
            when(setAmountCommandMock.newAmount()).thenReturn(newAmount);

            callCompletable = orderUtilCompletable.setAmount(setAmountCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setAmountCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewAmountIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setRequestedAmount(orderToSetAmount, newAmount);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setAmountCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewAmount() throws Exception {
            setUtilHandlerMockObservableForCommand(setAmountCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setAmountCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setAmountCommandMock, emptyObservable());
            setUpCommandToCompletable(setAmountCommandMock, OrderCallReason.CHANGE_AMOUNT);

            verify(orderUtilCompletable).setAmount(setAmountCommandMock);
        }
    }

    public class SetOpenPriceTests {

        private final IOrder orderToSetOpenPrice = buyOrderEURUSD;
        private final double newOpenPrice = 1.234;
        private final SetOpenPriceCommand setOpenPriceCommandMock = mock(SetOpenPriceCommand.class);

        @Before
        public void setUp() {
            when(setOpenPriceCommandMock.order()).thenReturn(orderToSetOpenPrice);
            when(setOpenPriceCommandMock.newOpenPrice()).thenReturn(newOpenPrice);

            callCompletable = orderUtilCompletable.setOpenPrice(setOpenPriceCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setOpenPriceCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewOpenPriceIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setOpenPrice(orderToSetOpenPrice, newOpenPrice);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setOpenPriceCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewOpenPrice() throws Exception {
            setUtilHandlerMockObservableForCommand(setOpenPriceCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setOpenPriceCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setOpenPriceCommandMock, emptyObservable());
            setUpCommandToCompletable(setOpenPriceCommandMock, OrderCallReason.CHANGE_PRICE);

            verify(orderUtilCompletable).setOpenPrice(setOpenPriceCommandMock);
        }
    }

    public class SetSLTests {

        private final IOrder orderToSetSL = buyOrderEURUSD;
        private final double newSL = 1.234;
        private final SetSLCommand setSLCommandMock = mock(SetSLCommand.class);

        @Before
        public void setUp() {
            when(setSLCommandMock.order()).thenReturn(orderToSetSL);
            when(setSLCommandMock.newSL()).thenReturn(newSL);

            callCompletable = orderUtilCompletable.setSL(setSLCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setSLCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewSLIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setSL(orderToSetSL, newSL);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setSLCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewSL() throws Exception {
            setUtilHandlerMockObservableForCommand(setSLCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setSLCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setSLCommandMock, emptyObservable());
            setUpCommandToCompletable(setSLCommandMock, OrderCallReason.CHANGE_SL);

            verify(orderUtilCompletable).setSL(setSLCommandMock);
        }
    }

    public class SetTPTests {

        private final IOrder orderToSetTP = buyOrderEURUSD;
        private final double newTP = 1.234;
        private final SetTPCommand setTPCommandMock = mock(SetTPCommand.class);

        @Before
        public void setUp() {
            when(setTPCommandMock.order()).thenReturn(orderToSetTP);
            when(setTPCommandMock.newTP()).thenReturn(newTP);

            callCompletable = orderUtilCompletable.setTP(setTPCommandMock);
        }

        @Test
        public void completableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(setTPCommandMock);
            verifyNoInteractionsToMocks();
        }

        @Test
        public void whenNewTPIsAlreadySetTheCallCompletesImmediately() throws Exception {
            orderUtilForTest.setTP(orderToSetTP, newTP);

            callCompletable.subscribe(completedActionMock);

            verify(completedActionMock).run();
            verify(orderUtilHandlerMock, never()).callObservable(setTPCommandMock);
        }

        @Test
        public void orderUtilHandlerIsCalledOnNewTP() throws Exception {
            setUtilHandlerMockObservableForCommand(setTPCommandMock, emptyObservable());

            callCompletable.subscribe(completedActionMock);

            verify(orderUtilHandlerMock).callObservable(setTPCommandMock);
            verify(completedActionMock).run();
        }

        @Test
        public void commandToCompletableMapsToCloseCall() {
            setUtilHandlerMockObservableForCommand(setTPCommandMock, emptyObservable());
            setUpCommandToCompletable(setTPCommandMock, OrderCallReason.CHANGE_TP);

            verify(orderUtilCompletable).setTP(setTPCommandMock);
        }
    }
}
