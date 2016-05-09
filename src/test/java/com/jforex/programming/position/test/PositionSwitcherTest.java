package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.uss;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionEvent;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import com.dukascopy.api.IEngine.OrderCommand;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionSwitcherTest extends InstrumentUtilForTest {

    private PositionSwitcher positionSwitcher;

    @Mock
    private Position positionMock;
    @Mock
    private OrderParamsSupplier orderParamsSupplierMock;
    @Captor
    private ArgumentCaptor<OrderParams> orderParamsCaptor;
    private final Subject<PositionEvent, PositionEvent> positionEventSubject = PublishSubject.create();
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSELL = OrderParamsForTest.paramsSellEURUSD();
    private final String buyLabel = orderParamsBUY.label();
    private final String sellLabel = orderParamsSELL.label();
    private final String buyMergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + buyLabel;
    private final String sellMergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + sellLabel;

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        positionSwitcher = new PositionSwitcher(positionMock, orderParamsSupplierMock);
    }

    private void setUpMocks() {
        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(orderParamsBUY);
        when(orderParamsSupplierMock.forCommand(OrderCommand.SELL)).thenReturn(orderParamsSELL);

        when(positionMock.positionEventObs()).thenReturn(positionEventSubject);
    }

    private void verifyNoPositionCommands() {
        verify(positionMock, never()).submit(any());
        verify(positionMock, never()).merge(any());
        verify(positionMock, never()).close();
    }

    private void verifySendedOrderParamsAreCorrect(final String mergeLabel,
                                                   final double expectedAmount,
                                                   final OrderCommand expectedCommand) {
        verify(positionMock).submit(orderParamsCaptor.capture());

        final OrderParams sendedOrderParams = orderParamsCaptor.getValue();
        assertThat(sendedOrderParams.label(), equalTo(mergeLabel));
        assertThat(sendedOrderParams.amount(), equalTo(expectedAmount));
        assertThat(sendedOrderParams.orderCommand(), equalTo(expectedCommand));
    }

    private void setPositionOrderDirection(final OrderDirection orderDirection) {
        when(positionMock.direction()).thenReturn(orderDirection);
    }

    private double expectedAmount(final double signedExposure,
                                  final OrderParams orderParams) {
        final double expectedAmount = MathUtil.roundAmount(orderParams.amount() + Math.abs(signedExposure));
        when(positionMock.signedExposure()).thenReturn(signedExposure);
        return expectedAmount;
    }

    @Test
    public void testSendFlatSignalWhenPositionIsFlatDoesNotCallPositionCommands() {
        setPositionOrderDirection(OrderDirection.FLAT);

        positionSwitcher.sendFlatSignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendBuySignalCallsSubmitWithCorrectAmountAndCommand() {
        setPositionOrderDirection(OrderDirection.SHORT);
        final double expectedAmount = expectedAmount(-0.12, orderParamsBUY);

        positionSwitcher.sendBuySignal();

        verifySendedOrderParamsAreCorrect(buyLabel, expectedAmount, OrderCommand.BUY);
    }

    @Test
    public void testSendSellSignalCallsSubmitWithCorrectAmountAndCommand() {
        setPositionOrderDirection(OrderDirection.LONG);
        final double expectedAmount = expectedAmount(0.03, orderParamsSELL);

        positionSwitcher.sendSellSignal();

        verifySendedOrderParamsAreCorrect(sellLabel, expectedAmount, OrderCommand.SELL);
    }

    @Test
    public void testOrderCommandIsCorrectedEvenIfParamProviderReturnedWrongCommand() {
        final OrderParams paramsWithWrongCommand = orderParamsBUY.clone()
                .withOrderCommand(OrderCommand.SELL)
                .build();

        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(paramsWithWrongCommand);
        setPositionOrderDirection(OrderDirection.FLAT);

        positionSwitcher.sendBuySignal();

        verifySendedOrderParamsAreCorrect(buyLabel, orderParamsBUY.amount(), OrderCommand.BUY);
    }

    public class BuySignal {

        @Before
        public void setUp() {
            setPositionOrderDirection(OrderDirection.FLAT);

            positionSwitcher.sendBuySignal();
        }

        @Test
        public void testSendedOrderParamsAreCorrect() {
            verifySendedOrderParamsAreCorrect(buyLabel, orderParamsBUY.amount(), OrderCommand.BUY);
        }

        public class SecondBuySignalIsIgnored {

            @Before
            public void setUp() {
                positionSwitcher.sendBuySignal();
            }

            @Test
            public void testOnlyOneSubmitCallSincePositionIsBuy() {
                verify(positionMock).submit(any());
            }
        }

        public class AfterSubmitOKMessage {

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.LONG);

                positionEventSubject.onNext(PositionEvent.SUBMITTASK_DONE);
            }

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(positionMock).merge(buyMergeLabel);
            }

            public class SecondBuySignalIsIgnored {

                @Before
                public void setUp() {
                    positionSwitcher.sendBuySignal();
                }

                @Test
                public void testOnlyOneSubmitCallSincePositionIsBuy() {
                    verify(positionMock).submit(any());
                }
            }

            @Test
            public void testSellSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendSellSignal();

                verify(positionMock).submit(any());
            }

            @Test
            public void testFlatSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendFlatSignal();

                verify(positionMock, never()).close();
            }

            public class AfterMergeOKMessage {

                @Before
                public void setUp() {
                    positionEventSubject.onNext(PositionEvent.MERGETASK_DONE);
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsLONG() {
                    positionSwitcher.sendBuySignal();

                    verify(positionMock).submit(any());
                }

                @Test
                public void testSellSignalIsNoLongerBlocked() {
                    positionSwitcher.sendSellSignal();

                    verify(positionMock, times(2)).submit(any());
                }

                @Test
                public void testFlatSingalIsNoLongerBlocked() {
                    positionSwitcher.sendFlatSignal();

                    verify(positionMock).close();
                }
            }
        }
    }

    /*
     * fhfffffffffffffffffffffffff
     */

    public class SellSignal {

        @Before
        public void setUp() {
            setPositionOrderDirection(OrderDirection.FLAT);

            positionSwitcher.sendSellSignal();
        }

        @Test
        public void testSendedOrderParamsAreCorrect() {
            verifySendedOrderParamsAreCorrect(sellLabel, orderParamsSELL.amount(), OrderCommand.SELL);
        }

        public class SecondSellSignalIsIgnored {

            @Before
            public void setUp() {
                positionSwitcher.sendSellSignal();
            }

            @Test
            public void testOnlyOneSubmitCallSincePositionIsBuy() {
                verify(positionMock).submit(any());
            }
        }

        public class AfterSubmitOKMessage {

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.SHORT);

                positionEventSubject.onNext(PositionEvent.SUBMITTASK_DONE);
            }

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(positionMock).merge(sellMergeLabel);
            }

            public class SecondSellSignalIsIgnored {

                @Before
                public void setUp() {
                    positionSwitcher.sendSellSignal();
                }

                @Test
                public void testOnlyOneSubmitCallSincePositionIsBuy() {
                    verify(positionMock).submit(any());
                }
            }

            @Test
            public void testSellSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendBuySignal();

                verify(positionMock).submit(any());
            }

            @Test
            public void testFlatSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendFlatSignal();

                verify(positionMock, never()).close();
            }

            public class AfterMergeOKMessage {

                @Before
                public void setUp() {
                    positionEventSubject.onNext(PositionEvent.MERGETASK_DONE);
                }

                @Test
                public void testSellSingalIsBlockedSincePositionIsSHORT() {
                    positionSwitcher.sendSellSignal();

                    verify(positionMock).submit(any());
                }

                @Test
                public void testBuySignalIsNoLongerBlocked() {
                    positionSwitcher.sendBuySignal();

                    verify(positionMock, times(2)).submit(any());
                }

                @Test
                public void testFlatSingalIsNoLongerBlocked() {
                    positionSwitcher.sendFlatSignal();

                    verify(positionMock).close();
                }
            }
        }
    }
}
