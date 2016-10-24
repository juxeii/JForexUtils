package com.jforex.programming.position.test;

import static com.jforex.programming.order.event.OrderEventTypeSets.allEvents;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionDirection;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        position = new Position(instrumentEURUSD, orderEventSubject);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     orderEventType,
                                                     true);
        orderEventSubject.onNext(orderEvent);
    }

    @Test
    public void createdOrdersAreAddedWhenInternal() {
        createEvents.forEach(eventType -> {
            sendOrderEvent(buyOrderEURUSD, eventType);
            assertTrue(position.contains(buyOrderEURUSD));
        });
    }

    @Test
    public void createdOrdersAreNotAddedWhenNotInternal() {
        createEvents.forEach(eventType -> {
            orderEventSubject.onNext(new OrderEvent(buyOrderEURUSD,
                                                    eventType,
                                                    false));
            assertFalse(position.contains(buyOrderEURUSD));
        });
    }

    @Test
    public void externalOrdersAreNotAdded() {
        allEvents.forEach(eventType -> {
            orderEventSubject.onNext(new OrderEvent(buyOrderEURUSD,
                                                    eventType,
                                                    false));
            assertFalse(position.contains(buyOrderEURUSD));
        });
    }

    @Test
    public void orderOfOtherPositionAreNotAdded() {
        orderEventSubject.onNext(submitEvent);
        assertFalse(position.contains(buyOrderAUDUSD));
    }

    public class AddingBuyOrder {

        @Before
        public void setUp() {
            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.OPENED);

            sendOrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_OK);
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
            assertThat(position.direction(), equalTo(PositionDirection.FLAT));
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
        public void testNoFilledOrders() {
            final Set<IOrder> filledOrders = position.filled();
            assertTrue(filledOrders.isEmpty());
        }

        @Test
        public void openOrdersContainsBuyOrder() {
            final Set<IOrder> openedOrders = position.opened();
            assertTrue(openedOrders.contains(buyOrderEURUSD));
        }

        @Test
        public void filledOrOpenedOrdersHasBuyOrder() {
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
                assertThat(position.direction(), equalTo(PositionDirection.LONG));
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
            public void openOrdersIsEmpty() {
                final Set<IOrder> openedOrders = position.opened();
                assertTrue(openedOrders.isEmpty());
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

                    sendOrderEvent(sellOrderEURUSD, OrderEventType.SUBMIT_OK);

                    logger.info(position.toString());
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
                    assertThat(position.direction(), equalTo(PositionDirection.SHORT));
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
                public void testFilledOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrders = position.filled();
                    assertTrue(filledOrders.contains(buyOrderEURUSD));
                    assertTrue(filledOrders.contains(sellOrderEURUSD));
                }

                @Test
                public void openOrdersIsEmpty() {
                    final Set<IOrder> openedOrders = position.opened();
                    assertTrue(openedOrders.isEmpty());
                }

                @Test
                public void testFilledOrOpenedOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrOpenedOrders = position.filledOrOpened();
                    assertTrue(filledOrOpenedOrders.contains(buyOrderEURUSD));
                    assertTrue(filledOrOpenedOrders.contains(sellOrderEURUSD));
                }

                @Test
                public void plInAccountCurrencyIsCorrect() {
                    orderUtilForTest.setPLInAccountCurrency(buyOrderEURUSD, 1233.5);
                    orderUtilForTest.setPLInAccountCurrency(sellOrderEURUSD, -3289.3);

                    assertThat(position.plInAccountCurrency(), equalTo(-2055.8));
                }

                @Test
                public void plInPipsIsCorrect() {
                    orderUtilForTest.setPLInPips(buyOrderEURUSD, 12.3);
                    orderUtilForTest.setPLInPips(sellOrderEURUSD, -3.0);

                    assertThat(position.plInPips(), equalTo(9.3));
                }

                public class OrdersAreProcessing {

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
                        assertThat(position.direction(), equalTo(PositionDirection.SHORT));
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

                    public class OrdersAreDone {

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
                            assertThat(position.direction(), equalTo(PositionDirection.SHORT));
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
                    assertThat(position.direction(), equalTo(PositionDirection.SHORT));

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
