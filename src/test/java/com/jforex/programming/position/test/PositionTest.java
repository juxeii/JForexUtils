package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() throws JFException {
        position = new Position(instrumentEURUSD, orderEventSubject);
    }

    public class AddingBuyOrder {

        private void sendOrderEvent(final IOrder order,
                                    final OrderEventType orderEventType) {
            final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
            orderEventSubject.onNext(orderEvent);
        }

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.OPENED);

            position.addOrder(buyOrderEURUSD);
        }

        @Test
        public void positionInstrumentIsCorrect() {
            assertThat(position.instrument(), equalTo(instrumentEURUSD));
        }

        @Test
        public void testPositionHasBuyOrder() {
            assertTrue(position.contains(buyOrderEURUSD));
        }

        @Test
        public void testPositionOrderSizeIsOne() {
            assertThat(position.size(), equalTo(1));
        }

        @Test
        public void testDirectionIsFLAT() {
            assertThat(position.direction(), equalTo(OrderDirection.FLAT));
        }

        @Test
        public void testNoExposure() {
            assertThat(position.signedExposure(), equalTo(0.0));
        }

        @Test
        public void testOrdersHasBuyOrder() {
            final Set<IOrder> orders = position.all();
            assertTrue(orders.contains(buyOrderEURUSD));
        }

        @Test
        public void testFilterWorksCorrect() {
            final Set<IOrder> orderFilter =
                    position.filter(order -> order.getLabel().equals(buyOrderEURUSD.getLabel()));
            assertTrue(orderFilter.contains(buyOrderEURUSD));
        }

        @Test
        public void testBuyOrderIsNotProcessing() {
            final Set<IOrder> notProcessingOrders =
                    position.notProcessingOrders(order -> true);
            assertTrue(notProcessingOrders.contains(buyOrderEURUSD));
        }

        @Test
        public void testNoFilledOrders() {
            final Set<IOrder> filledOrders = position.filled();
            assertTrue(filledOrders.isEmpty());
        }

        @Test
        public void testNoFilledOrOpenedOrdersHasBuyOrder() {
            final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
            assertTrue(filledOrOpenedOrders.contains(buyOrderEURUSD));
        }

        public class BuyOrderIsFilled {

            @Before
            public void setUp() {
                orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
            }

            @Test
            public void testDirectionIsLONG() {
                assertThat(position.direction(), equalTo(OrderDirection.LONG));
            }

            @Test
            public void testSignedExposureIsPlus() {
                assertThat(position.signedExposure(), equalTo(buyOrderEURUSD.getAmount()));
            }

            @Test
            public void testFilledOrdersHasBuyOrder() {
                final Set<IOrder> filledOrders = position.filled();
                assertTrue(filledOrders.contains(buyOrderEURUSD));
            }

            @Test
            public void testFilledOrOpenedOrdersHasBuyOrder() {
                final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                assertTrue(filledOrOpenedOrders.contains(buyOrderEURUSD));
            }

            public class AddingSellOrder {

                @Before
                public void setUp() {
                    orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.FILLED);

                    position.addOrder(sellOrderEURUSD);
                }

                @Test
                public void testPositionHasBuyAndSellOrder() {
                    assertTrue(position.contains(buyOrderEURUSD));
                    assertTrue(position.contains(sellOrderEURUSD));
                }

                @Test
                public void testPositionOrderSizeIsTwo() {
                    assertThat(position.size(), equalTo(2));
                }

                @Test
                public void testDirectionIsShortSinceSellAmountIsBigger() {
                    assertThat(position.direction(), equalTo(OrderDirection.SHORT));
                }

                @Test
                public void testExposureIsSignedAmount() {
                    final double buyAmount = OrderStaticUtil.signedAmount(buyOrderEURUSD);
                    final double sellAmount = OrderStaticUtil.signedAmount(sellOrderEURUSD);
                    assertThat(position.signedExposure(), equalTo(buyAmount + sellAmount));
                }

                @Test
                public void testOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> orders = position.all();
                    assertTrue(orders.contains(buyOrderEURUSD));
                    assertTrue(orders.contains(sellOrderEURUSD));
                }

                @Test
                public void testOrdersAreNotProcessing() {
                    final Set<IOrder> notProcessingOrders =
                            position.notProcessingOrders(order -> true);
                    assertTrue(notProcessingOrders.contains(buyOrderEURUSD));
                    assertTrue(notProcessingOrders.contains(sellOrderEURUSD));
                }

                @Test
                public void testFilledOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrders = position.filled();
                    assertTrue(filledOrders.contains(buyOrderEURUSD));
                    assertTrue(filledOrders.contains(sellOrderEURUSD));
                }

                @Test
                public void testFilledOrOpenedOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                    assertTrue(filledOrOpenedOrders.contains(buyOrderEURUSD));
                    assertTrue(filledOrOpenedOrders.contains(sellOrderEURUSD));
                }

                @Test
                public void testMarkingOrdersActiveOnlyAffectsPassedOrders() {
                    position.markOrdersActive(Sets.newHashSet(buyOrderEURUSD, sellOrderAUDUSD));

                    final Set<IOrder> notProcessingOrders =
                            position.notProcessingOrders(order -> true);
                    assertThat(notProcessingOrders.size(), equalTo(1));
                    assertTrue(notProcessingOrders.contains(sellOrderEURUSD));
                }

                public class MarkingOrdersActive {

                    @Before
                    public void setUp() {
                        position.markOrdersActive(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                    }

                    @Test
                    public void testPositionHasBuyAndSellOrder() {
                        assertTrue(position.contains(buyOrderEURUSD));
                        assertTrue(position.contains(sellOrderEURUSD));
                    }

                    @Test
                    public void testPositionOrderSizeIsTwo() {
                        assertThat(position.size(), equalTo(2));
                    }

                    @Test
                    public void testDirectionIsShortSinceSellAmountIsBigger() {
                        assertThat(position.direction(), equalTo(OrderDirection.SHORT));
                    }

                    @Test
                    public void testExposureIsSignedAmount() {
                        final double buyAmount = OrderStaticUtil.signedAmount(buyOrderEURUSD);
                        final double sellAmount = OrderStaticUtil.signedAmount(sellOrderEURUSD);
                        assertThat(position.signedExposure(), equalTo(buyAmount + sellAmount));
                    }

                    @Test
                    public void testOrdersHasBuyAndSellOrder() {
                        final Set<IOrder> orders = position.all();
                        assertTrue(orders.contains(buyOrderEURUSD));
                        assertTrue(orders.contains(sellOrderEURUSD));
                    }

                    @Test
                    public void testOrdersAreProcessing() {
                        final Set<IOrder> notProcessingOrders =
                                position.notProcessingOrders(order -> true);
                        assertTrue(notProcessingOrders.isEmpty());
                    }

                    @Test
                    public void testFilledOrdersIsEmptySinceAllActive() {
                        final Set<IOrder> filledOrders = position.filled();
                        assertTrue(filledOrders.isEmpty());
                    }

                    @Test
                    public void testFilledOrOpenedOrdersISEmptySinceAllActive() {
                        final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                        assertTrue(filledOrOpenedOrders.isEmpty());
                    }

                    @Test
                    public void testCloseOnTPRemovesOrderAlsoWhenMarkedActive() {
                        orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.CLOSED);
                        sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_TP);

                        assertFalse(position.contains(sellOrderEURUSD));
                    }

                    @Test
                    public void testCloseOnSLRemovesOrderAlsoWhenMarkedActive() {
                        orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.CLOSED);
                        sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_SL);

                        assertFalse(position.contains(sellOrderEURUSD));
                    }

                    @Test
                    public void testMarkingOrdersIdleOnlyAffectsPassedOrders() {
                        position.markOrdersIdle(Sets.newHashSet(buyOrderEURUSD, sellOrderAUDUSD));

                        final Set<IOrder> notProcessingOrders =
                                position.notProcessingOrders(order -> true);
                        assertThat(notProcessingOrders.size(), equalTo(1));
                        assertTrue(notProcessingOrders.contains(buyOrderEURUSD));
                    }

                    public class MarkingOrdersIDLE {

                        @Before
                        public void setUp() {
                            position.markOrdersIdle(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(position.contains(buyOrderEURUSD));
                            assertTrue(position.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void testPositionOrderSizeIsTwo() {
                            assertThat(position.size(), equalTo(2));
                        }

                        @Test
                        public void testDirectionIsShortSinceSellAmountIsBigger() {
                            assertThat(position.direction(), equalTo(OrderDirection.SHORT));
                        }

                        @Test
                        public void testExposureIsSignedAmount() {
                            final double buyAmount = OrderStaticUtil.signedAmount(buyOrderEURUSD);
                            final double sellAmount = OrderStaticUtil.signedAmount(sellOrderEURUSD);
                            assertThat(position.signedExposure(), equalTo(buyAmount + sellAmount));
                        }

                        @Test
                        public void testOrdersHasBuyAndSellOrder() {
                            final Set<IOrder> orders = position.all();
                            assertTrue(orders.contains(buyOrderEURUSD));
                            assertTrue(orders.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void testOrdersAreIDLE() {
                            final Set<IOrder> notProcessingOrders =
                                    position.notProcessingOrders(order -> true);
                            assertTrue(notProcessingOrders.contains(buyOrderEURUSD));
                            assertTrue(notProcessingOrders.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void filledOrdersContainsBuyAndSellOrderSinceAllAreIDLE() {
                            final Set<IOrder> filledOrders = position.filled();
                            assertTrue(filledOrders.contains(buyOrderEURUSD));
                            assertTrue(filledOrders.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void filledOrOpenedOrdersContainsBuyAndSellOrderSinceAllAreIDLE() {
                            final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                            assertTrue(filledOrOpenedOrders.contains(buyOrderEURUSD));
                            assertTrue(filledOrOpenedOrders.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void testCloseOnTPRemovesOrderAlsoWhenMarkedActive() {
                            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.CLOSED);
                            sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_TP);

                            assertFalse(position.contains(sellOrderEURUSD));
                        }

                        @Test
                        public void testCloseOnSLRemovesOrderAlsoWhenMarkedActive() {
                            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.CLOSED);
                            sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_SL);

                            assertFalse(position.contains(sellOrderEURUSD));
                        }
                    }
                }

                private void assertBuyOrderRemoval(final OrderEventType orderEventType) {
                    sendOrderEvent(buyOrderEURUSD, orderEventType);

                    assertFalse(position.contains(buyOrderEURUSD));
                    assertThat(position.size(), equalTo(1));
                    assertThat(position.direction(), equalTo(OrderDirection.SHORT));

                    final double sellAmount = OrderStaticUtil.signedAmount(sellOrderEURUSD);
                    assertThat(position.signedExposure(), equalTo(sellAmount));

                    final Set<IOrder> orders = position.all();
                    assertTrue(orders.contains(sellOrderEURUSD));

                    final Set<IOrder> filledOrders = position.filled();
                    assertTrue(filledOrders.contains(sellOrderEURUSD));

                    final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                    assertTrue(filledOrOpenedOrders.contains(sellOrderEURUSD));
                }

                public class RemovingEventsWhenOrderIsClosed {

                    @Before
                    public void setUp() {
                        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CLOSED);
                    }

                    @Test
                    public void testCloseOK() {
                        assertBuyOrderRemoval(OrderEventType.CLOSE_OK);
                    }

                    @Test
                    public void testCloseOnSL() {
                        assertBuyOrderRemoval(OrderEventType.CLOSED_BY_SL);
                    }

                    @Test
                    public void testCloseOnTP() {
                        assertBuyOrderRemoval(OrderEventType.CLOSED_BY_TP);
                    }

                    @Test
                    public void testClosedByMergeOK() {
                        assertBuyOrderRemoval(OrderEventType.CLOSED_BY_MERGE);
                    }

                    @Test
                    public void testClosedOnMergeOK() {
                        assertBuyOrderRemoval(OrderEventType.MERGE_CLOSE_OK);
                    }
                }

                @Test
                public void RemovingEventsWhenOrderIsCanceledIsCloseOK() {
                    orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);

                    assertBuyOrderRemoval(OrderEventType.FILL_REJECTED);
                }
            }
        }
    }
}