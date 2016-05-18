package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import com.jforex.programming.misc.JFObservable;
import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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
    private final Subject<OrderEvent, OrderEvent> submitObs = PublishSubject.create();
    private final JFObservable<Long> mergeCompleter = new JFObservable<>();
    private final JFObservable<Long> closeCompleter = new JFObservable<>();
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSELL = OrderParamsForTest.paramsSellEURUSD();
    private final String buyLabel = orderParamsBUY.label();
    private final String sellLabel = orderParamsSELL.label();
    private final String buyMergeLabel = userSettings.defaultMergePrefix() + buyLabel;
    private final String sellMergeLabel = userSettings.defaultMergePrefix() + sellLabel;

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        positionSwitcher = new PositionSwitcher(instrumentEURUSD,
                                                orderUtilMock,
                                                orderParamsSupplierMock);
    }

    private void setUpMocks() {
        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(orderParamsBUY);
        when(orderParamsSupplierMock.forCommand(OrderCommand.SELL)).thenReturn(orderParamsSELL);

        when(orderUtilMock.position(instrumentEURUSD)).thenReturn(positionMock);
        when(orderUtilMock.submitOrder(any())).thenReturn(submitObs.take(1));
        when(orderUtilMock.mergePositionOrders(buyMergeLabel, instrumentEURUSD))
                .thenReturn(Completable.fromObservable(mergeCompleter.get().take(1)));
        when(orderUtilMock.mergePositionOrders(sellMergeLabel, instrumentEURUSD))
                .thenReturn(Completable.fromObservable(mergeCompleter.get().take(1)));
        when(orderUtilMock.closePosition(instrumentEURUSD))
                .thenReturn(Completable.fromObservable(closeCompleter.get().take(1)));
    }

    private void verifyNoOrderUtilCalls() {
        verify(orderUtilMock, never()).submitOrder(any());
        verify(orderUtilMock, never()).mergePositionOrders(anyString(), any());
        verify(orderUtilMock, never()).closePosition(any());
    }

    private void verifySendedOrderParamsAreCorrect(final String mergeLabel,
                                                   final double expectedAmount,
                                                   final OrderCommand expectedCommand) {
        verify(orderUtilMock).submitOrder(orderParamsCaptor.capture());

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

        verifyNoOrderUtilCalls();
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
                verify(orderUtilMock).submitOrder(any());
            }
        }

        public class AfterSubmitCompleted {

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.LONG);

                submitObs.onCompleted();
            }

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(orderUtilMock).mergePositionOrders(buyMergeLabel, instrumentEURUSD);
            }

            public class SecondBuySignalIsIgnored {

                @Before
                public void setUp() {
                    positionSwitcher.sendBuySignal();
                }

                @Test
                public void testOnlyOneSubmitCallSincePositionIsBuy() {
                    verify(orderUtilMock).submitOrder(any());
                }
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

            public class AfterMergeCompleted {

                @Before
                public void setUp() {
                    mergeCompleter.onNext(1L);
                }

                @Test
                public void testBuySingalIsBlockedSincePositionIsLONG() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testSellSignalIsNoLongerBlocked() {
                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock, times(2)).submitOrder(any());
                }

                public class AfterClose {

                    @Before
                    public void setUp() {
                        positionSwitcher.sendFlatSignal();
                    }

                    @Test
                    public void testCloseIsCalledOnPosition() {
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

                    public class AfterCloseCompleted {

                        @Before
                        public void setUp() {
                            setPositionOrderDirection(OrderDirection.FLAT);

                            closeCompleter.onNext(1L);
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
                    }
                }
            }
        }
    }

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
                verify(orderUtilMock).submitOrder(any());
            }
        }

        public class AfterSubmitCompleted {

            @Before
            public void setUp() {
                setPositionOrderDirection(OrderDirection.SHORT);

                submitObs.onCompleted();
            }

            @Test
            public void testMergeIsCalledOnPosition() {
                verify(orderUtilMock).mergePositionOrders(sellMergeLabel, instrumentEURUSD);
            }

            public class SecondSellSignalIsIgnored {

                @Before
                public void setUp() {
                    positionSwitcher.sendSellSignal();
                }

                @Test
                public void testOnlyOneSubmitCallSincePositionIsBuy() {
                    verify(orderUtilMock).submitOrder(any());
                }
            }

            @Test
            public void testSellSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendBuySignal();

                verify(orderUtilMock).submitOrder(any());
            }

            @Test
            public void testFlatSingalIsBlockedSincePositionIsBusy() {
                positionSwitcher.sendFlatSignal();

                verify(orderUtilMock, never()).closePosition(instrumentEURUSD);
            }

            public class AfterMergeCompleted {

                @Before
                public void setUp() {
                    mergeCompleter.onNext(1L);
                }

                @Test
                public void testSellSingalIsBlockedSincePositionIsSHORT() {
                    positionSwitcher.sendSellSignal();

                    verify(orderUtilMock).submitOrder(any());
                }

                @Test
                public void testBuySignalIsNoLongerBlocked() {
                    positionSwitcher.sendBuySignal();

                    verify(orderUtilMock, times(2)).submitOrder(any());
                }

                @Test
                public void testFlatSingalIsNoLongerBlocked() {
                    positionSwitcher.sendFlatSignal();

                    verify(orderUtilMock).closePosition(instrumentEURUSD);
                }

                public class AfterCloseCompleted {

                    @Before
                    public void setUp() {
                        setPositionOrderDirection(OrderDirection.FLAT);

                        closeCompleter.onNext(1L);
                    }

                    @Test
                    public void testBuySignalIsNoLonerBlocked() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }

                    @Test
                    public void testuySignalIsNoLonerBlocked() {
                        positionSwitcher.sendBuySignal();

                        verify(orderUtilMock, times(2)).submitOrder(any());
                    }
                }
            }
        }
    }
}
