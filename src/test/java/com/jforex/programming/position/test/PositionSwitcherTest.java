package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.NoRestorePolicy;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Observable;

@RunWith(HierarchicalContextRunner.class)
public class PositionSwitcherTest extends InstrumentUtilForTest {

    private PositionSwitcher positionSwitcher;

    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private Position positionMock;
    @Mock
    private OrderParamsSupplier orderParamsSupplierMock;
    @Captor
    private ArgumentCaptor<OrderParams> orderParamsCaptor;
    private final RestoreSLTPPolicy noRestoreSLTPPolicy = new NoRestorePolicy();
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSELL = OrderParamsForTest.paramsSellEURUSD();
    private final String buyLabel = orderParamsBUY.label();
    private final String sellLabel = orderParamsSELL.label();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();
        setPositionOrderDirection(OrderDirection.FLAT);

        positionSwitcher = new PositionSwitcher(instrumentEURUSD,
                                                orderUtilMock,
                                                orderParamsSupplierMock);
    }

    private void setUpMocks() {
        when(orderUtilMock.position(instrumentEURUSD)).thenReturn(positionMock);
        when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(Completable.complete());

        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(orderParamsBUY);
        when(orderParamsSupplierMock.forCommand(OrderCommand.SELL)).thenReturn(orderParamsSELL);
    }

    private void verifyNoPositionCommands() {
        verify(positionMock, never()).merge(any(), eq(noRestoreSLTPPolicy));
        verify(positionMock, never()).close();
    }

    private void verifySendedOrderParamsAreCorrect(final String mergeLabel,
                                                   final double expectedAmount,
                                                   final OrderCommand expectedCommand) {
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

    public class FlatSetup {

        @Test
        public void testSendedOrderParamsAreCorrect() {
            final OrderParams paramsWithWrongCommand = orderParamsBUY.clone()
                    .withOrderCommand(OrderCommand.SELL)
                    .build();

            when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(paramsWithWrongCommand);

            setPositionOrderDirection(OrderDirection.FLAT);

            positionSwitcher.sendFlatSignal();
        }

        @Test
        public void testFlatSignalIsIgnored() {
            verifyNoPositionCommands();
        }

        @Test
        public void testOrderCommandIsCorrectedEvenIfParamProviderReturnedWrongCommand() {
            positionSwitcher.sendBuySignal();

            verify(orderUtilMock).submitOrder(orderParamsCaptor.capture());
            verifySendedOrderParamsAreCorrect(buyLabel, orderParamsBUY.amount(), OrderCommand.BUY);
        }
    }

    public class BuySetup {

        @Test
        public void testSendedOrderParamsAreCorrect() {
            positionSwitcher.sendBuySignal();

            verify(orderUtilMock).submitOrder(orderParamsCaptor.capture());
            verifySendedOrderParamsAreCorrect(buyLabel, orderParamsBUY.amount(), OrderCommand.BUY);
        }

        public class WhenSubmitIsBusy {

            @Before
            public void setUp() {
                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.never());
            }

            @Test
            public void testSellSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendSellSignal();

                verify(orderUtilMock).submitOrder(any());
            }

            @Test
            public void testFlatSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendFlatSignal();

                verify(orderUtilMock, never()).closePosition(instrumentEURUSD);
            }
        }

        public class WhenSubmitCompleted {

            private final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.LONG);

                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
            }

            public class WhenMergeIsBusy {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(buyMergeLabel, instrumentEURUSD, noRestoreSLTPPolicy))
                            .thenReturn(Completable.never());
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSellSingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testFlatSingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendFlatSignal();

                    verify(orderUtilMock, never()).closePosition(instrumentEURUSD);
                }
            }

            public class WhenMergeCompleted {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel), eq(instrumentEURUSD), any()))
                            .thenReturn(Completable.complete());

                    positionSwitcher.sendBuySignal();
                }

                @Test
                public void testSubmitWasCalledOnOrderUtil() {
                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testMergeWasCalledOnOrderUtil() {
                    verify(orderUtilMock).mergePositionOrders(eq(buyMergeLabel), eq(instrumentEURUSD), any());
                }

                @Test
                public void testBuySignalIsIgnoredInLongState() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSendSellSignalCallsSubmitWithCorrectAmountAndCommand() {
                    final double expectedAmount = expectedAmount(0.03, orderParamsSELL);

                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock, times(2)).submitOrder(orderParamsCaptor.capture());
                    verifySendedOrderParamsAreCorrect(sellLabel, expectedAmount, OrderCommand.SELL);
                }

                public class WhenCloseIsBusy {

                    @Before
                    public void setUp() {
                        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(Completable.never());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testFlatSignalIsPermitted() {
                        positionSwitcher.sendFlatSignal();

                        verify(orderUtilMock).closePosition(instrumentEURUSD);
                    }

                    @Test
                    public void testBuySingalIsBlockedSincePositionIsBusy() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock).submitOrder(any());
                    }

                    @Test
                    public void testSellSingalIsBlockedSincePositionIsBusy() {
                        positionSwitcher.sendSellSignal();

                        verify(orderUtilMock).submitOrder(any());
                    }
                }

                public class WhenCloseCompleted {

                    @Before
                    public void setUp() {
                        setPositionOrderDirection(OrderDirection.FLAT);

                        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(Completable.complete());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testBuySignalIsNoLonerBlocked() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testSellSignalIsNoLonerBlocked() {
                        positionSwitcher.sendSellSignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testFlatSingalIsBlockedSincePositionIsFLAT() {
                        positionSwitcher.sendFlatSignal();

                        verify(orderUtilMock).closePosition(instrumentEURUSD);
                    }
                }
            }
        }
    }

    public class SellSetup {

        @Test
        public void testSendedOrderParamsAreCorrect() {
            positionSwitcher.sendSellSignal();

            verify(orderUtilMock).submitOrder(orderParamsCaptor.capture());
            verifySendedOrderParamsAreCorrect(sellLabel, orderParamsSELL.amount(), OrderCommand.SELL);
        }

        public class WhenSubmitIsBusy {

            @Before
            public void setUp() {
                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.never());
            }

            @Test
            public void testSellSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendSellSignal();

                verify(orderUtilMock).submitOrder(any());
            }

            @Test
            public void testBuySingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendBuySignal();

                verify(orderUtilMock).submitOrder(any());
            }

            @Test
            public void testFlatSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendFlatSignal();

                verify(orderUtilMock, never()).closePosition(instrumentEURUSD);
            }
        }

        public class WhenSubmitCompleted {

            private final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.SHORT);

                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
            }

            public class WhenMergeIsBusy {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(sellMergeLabel, instrumentEURUSD, noRestoreSLTPPolicy))
                            .thenReturn(Completable.never());
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testFlatSingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendFlatSignal();

                    verify(orderUtilMock, never()).closePosition(instrumentEURUSD);
                }
            }

            public class WhenMergeCompleted {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel), eq(instrumentEURUSD), any()))
                            .thenReturn(Completable.complete());

                    positionSwitcher.sendSellSignal();
                }

                @Test
                public void testSubmitWasCalledOnOrderUtil() {
                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testMergeWasCalledOnOrderUtil() {
                    verify(orderUtilMock).mergePositionOrders(eq(sellMergeLabel), eq(instrumentEURUSD), any());
                }

                @Test
                public void testSellSignalIsIgnoredInLongState() {
                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSendBuySignalCallsSubmitWithCorrectAmountAndCommand() {
                    final double expectedAmount = expectedAmount(-0.12, orderParamsBUY);

                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock, times(2)).submitOrder(orderParamsCaptor.capture());
                    verifySendedOrderParamsAreCorrect(buyLabel, expectedAmount, OrderCommand.BUY);
                }

                public class WhenCloseIsBusy {

                    @Before
                    public void setUp() {
                        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(Completable.never());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testFlatSignalIsPermitted() {
                        positionSwitcher.sendFlatSignal();

                        verify(orderUtilMock).closePosition(instrumentEURUSD);
                    }

                    @Test
                    public void testBuySingalIsBlockedSincePositionIsBusy() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock).submitOrder(any());
                    }

                    @Test
                    public void testSellSingalIsBlockedSincePositionIsBusy() {
                        positionSwitcher.sendSellSignal();

                        verify(orderUtilMock).submitOrder(any());
                    }
                }

                public class WhenCloseCompleted {

                    @Before
                    public void setUp() {
                        setPositionOrderDirection(OrderDirection.FLAT);

                        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(Completable.complete());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testBuySignalIsNoLonerBlocked() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testSellSignalIsNoLonerBlocked() {
                        positionSwitcher.sendSellSignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testFlatSingalIsBlockedSincePositionIsFLAT() {
                        positionSwitcher.sendFlatSignal();

                        verify(orderUtilMock).closePosition(instrumentEURUSD);
                    }
                }
            }
        }
    }
}
