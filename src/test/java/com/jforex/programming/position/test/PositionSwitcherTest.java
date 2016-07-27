package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;

@RunWith(HierarchicalContextRunner.class)
public class PositionSwitcherTest extends InstrumentUtilForTest {

    private PositionSwitcher positionSwitcher;

    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private OrderParamsSupplier orderParamsSupplierMock;
    @Captor
    private ArgumentCaptor<OrderParams> paramsCaptor;
    private final OrderParams orderParamsBUY = IOrderForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSELL = IOrderForTest.paramsSellEURUSD();
    private final String buyLabel = orderParamsBUY.label();
    private final String sellLabel = orderParamsSELL.label();

    @Before
    public void setUp() {
        setUpMocks();
        setPositionOrderDirection(OrderDirection.FLAT);

        positionSwitcher = new PositionSwitcher(instrumentEURUSD,
                                                orderUtilMock,
                                                orderParamsSupplierMock);
    }

    private void setUpMocks() {
        when(orderUtilMock.positionOrders(instrumentEURUSD)).thenReturn(positionOrdersMock);
        when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
        when(orderUtilMock.closePosition(instrumentEURUSD)).thenReturn(emptyObservable());

        when(orderParamsSupplierMock.get(OrderCommand.BUY)).thenReturn(orderParamsBUY);
        when(orderParamsSupplierMock.get(OrderCommand.SELL)).thenReturn(orderParamsSELL);
    }

    private void setPositionOrderDirection(final OrderDirection orderDirection) {
        when(positionOrdersMock.direction()).thenReturn(orderDirection);
    }

    private double expectedAmount(final double signedExposure,
                                  final OrderParams orderParams) {
        final double expectedAmount =
                MathUtil.roundAmount(orderParams.amount() + Math.abs(signedExposure));
        when(positionOrdersMock.signedExposure()).thenReturn(signedExposure);
        return expectedAmount;
    }

    private void verifySendedOrderParams(final String label,
                                         final OrderCommand orderCommand,
                                         final double amount,
                                         final int times) {
        verify(orderUtilMock, times(times)).submitOrder(paramsCaptor.capture());
        final OrderParams params = paramsCaptor.getValue();

        assertThat(params.label(), equalTo(label));
        assertThat(params.orderCommand(), equalTo(orderCommand));
        assertThat(params.amount(), equalTo(amount));
    }

    public class FlatSetup {

        @Test
        public void testSendedOrderParamsAreCorrect() {
            final OrderParams paramsWithWrongCommand = orderParamsBUY.clone()
                    .withOrderCommand(OrderCommand.SELL)
                    .build();

            when(orderParamsSupplierMock.get(OrderCommand.BUY))
                    .thenReturn(paramsWithWrongCommand);

            setPositionOrderDirection(OrderDirection.FLAT);

            positionSwitcher.sendFlatSignal();
        }

        @Test
        public void testOrderCommandIsCorrectedEvenIfParamProviderReturnedWrongCommand() {
            final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;
            when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                   eq(instrumentEURUSD))).thenReturn(Observable.empty());

            positionSwitcher.sendBuySignal();
            verifySendedOrderParams(buyLabel, OrderCommand.BUY, orderParamsBUY.amount(), 1);
        }
    }

    public class BuySetup {

        private final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;

        @Test
        public void testSendedOrderParamsAreCorrect() {
            when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                   eq(instrumentEURUSD))).thenReturn(Observable.empty());

            positionSwitcher.sendBuySignal();

            verifySendedOrderParams(buyLabel, OrderCommand.BUY, orderParamsBUY.amount(), 1);
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

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.LONG);

                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
            }

            public class WhenMergeIsBusy {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.never());
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsBusy() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSellSingalIsBlockedSincePositionIsBusy() {
                    final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;
                    when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());

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
                    when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());

                    positionSwitcher.sendBuySignal();
                }

                @Test
                public void testSubmitWasCalledOnOrderUtil() {
                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testMergeWasCalledOnOrderUtil() {
                    verify(orderUtilMock).mergePositionOrders(eq(buyMergeLabel),
                                                              eq(instrumentEURUSD));
                }

                @Test
                public void testBuySignalIsIgnoredInLongState() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSendSellSignalCallsSubmitWithCorrectAmountAndCommand() {
                    final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;
                    when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());
                    final double expectedAmount = expectedAmount(0.03, orderParamsSELL);

                    positionSwitcher.sendSellSignal();

                    verifySendedOrderParams(sellLabel, OrderCommand.SELL, expectedAmount, 2);
                }

                public class WhenCloseIsBusy {

                    @Before
                    public void setUp() {
                        when(orderUtilMock.closePosition(instrumentEURUSD))
                                .thenReturn(Observable.never());

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

                        when(orderUtilMock.closePosition(instrumentEURUSD))
                                .thenReturn(emptyObservable());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testBuySignalIsNoLonerBlocked() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testSellSignalIsNoLonerBlocked() {
                        final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;
                        when(orderUtilMock
                                .mergePositionOrders(eq(sellMergeLabel),
                                                     eq(instrumentEURUSD))).thenReturn(Observable.empty());

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

        private final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;

        @Test
        public void testSendedOrderParamsAreCorrect() {
            when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel),
                                                   eq(instrumentEURUSD))).thenReturn(Observable.empty());

            positionSwitcher.sendSellSignal();
            verifySendedOrderParams(sellLabel, OrderCommand.SELL, orderParamsSELL.amount(), 1);
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

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.SHORT);

                when(orderUtilMock.submitOrder(any())).thenReturn(Observable.empty());
            }

            public class WhenMergeIsBusy {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.never());
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsBusy() {
                    final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;
                    when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());
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
                    when(orderUtilMock.mergePositionOrders(eq(sellMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());

                    positionSwitcher.sendSellSignal();
                }

                @Test
                public void testSubmitWasCalledOnOrderUtil() {
                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testMergeWasCalledOnOrderUtil() {
                    verify(orderUtilMock).mergePositionOrders(eq(sellMergeLabel),
                                                              eq(instrumentEURUSD));
                }

                @Test
                public void testSellSignalIsIgnoredInLongState() {
                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSendBuySignalCallsSubmitWithCorrectAmountAndCommand() {
                    final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;
                    when(orderUtilMock.mergePositionOrders(eq(buyMergeLabel),
                                                           eq(instrumentEURUSD))).thenReturn(Observable.empty());
                    final double expectedAmount = expectedAmount(-0.12, orderParamsBUY);

                    positionSwitcher.sendBuySignal();

                    verifySendedOrderParams(buyLabel, OrderCommand.BUY, expectedAmount, 2);
                }

                public class WhenCloseIsBusy {

                    @Before
                    public void setUp() {
                        when(orderUtilMock.closePosition(instrumentEURUSD))
                                .thenReturn(Observable.never());

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

                        when(orderUtilMock.closePosition(instrumentEURUSD))
                                .thenReturn(emptyObservable());

                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testBuySignalIsNoLonerBlocked() {
                        final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;
                        when(orderUtilMock
                                .mergePositionOrders(eq(buyMergeLabel),
                                                     eq(instrumentEURUSD))).thenReturn(Observable.empty());

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
