package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        position = new Position(instrumentEURUSD, orderEventSubject);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
        orderEventSubject.onNext(orderEvent);
    }

    @Test
    public void testAddingWrongOrderInstrumentIsIgnored() {
        final IOrder wrongOrder = IOrderForTest.orderAUDUSD();

        position.addOrder(IOrderForTest.orderAUDUSD());

        assertFalse(position.contains(wrongOrder));
    }

    public class AddingBuyOrder {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.OPENED);

            position.addOrder(buyOrder);
        }

        @Test
        public void testPositionHasBuyOrder() {
            assertTrue(position.contains(buyOrder));
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
            final Set<IOrder> orders = position.orders();
            assertTrue(orders.contains(buyOrder));
        }

        @Test
        public void testFilterWorksCorrect() {
            final Set<IOrder> orderFilter =
                    position.filterOrders(order -> order.getLabel().equals(buyOrder.getLabel()));
            assertTrue(orderFilter.contains(buyOrder));
        }

        @Test
        public void testBuyOrderIsNotProcessing() {
            final Set<IOrder> notProcessingOrders =
                    position.notProcessingOrders(order -> true);
            assertTrue(notProcessingOrders.contains(buyOrder));
        }

        @Test
        public void testNoFilledOrders() {
            final Set<IOrder> filledOrders = position.filledOrders();
            assertTrue(filledOrders.isEmpty());
        }

        @Test
        public void testNoFilledOrOpenedOrdersHasBuyOrder() {
            final Set<IOrder> filledOrOpenedOrders = position.filledOrOpenedOrders();
            assertTrue(filledOrOpenedOrders.contains(buyOrder));
        }

        public class BuyOrderIsFilled {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.FILLED);
            }

            @Test
            public void testDirectionIsLONG() {
                assertThat(position.direction(), equalTo(OrderDirection.LONG));
            }

            @Test
            public void testSignedExposureIsPlus() {
                assertThat(position.signedExposure(), equalTo(buyOrder.getAmount()));
            }

            @Test
            public void testFilledOrdersHasBuyOrder() {
                final Set<IOrder> filledOrders = position.filledOrders();
                assertTrue(filledOrders.contains(buyOrder));
            }

            @Test
            public void testFilledOrOpenedOrdersHasBuyOrder() {
                final Set<IOrder> filledOrOpenedOrders = position.filledOrOpenedOrders();
                assertTrue(filledOrOpenedOrders.contains(buyOrder));
            }

            public class AddingSellOrder {

                private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();

                @Before
                public void setUp() {
                    sellOrder.setState(IOrder.State.FILLED);

                    position.addOrder(sellOrder);
                }

                @Test
                public void testPositionHasBuyAndSellOrder() {
                    assertTrue(position.contains(buyOrder));
                    assertTrue(position.contains(sellOrder));
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
                    final double buyAmount = OrderStaticUtil.signedAmount(buyOrder);
                    final double sellAmount = OrderStaticUtil.signedAmount(sellOrder);
                    assertThat(position.signedExposure(), equalTo(buyAmount + sellAmount));
                }

                @Test
                public void testOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> orders = position.orders();
                    assertTrue(orders.contains(buyOrder));
                    assertTrue(orders.contains(sellOrder));
                }

                @Test
                public void testOrdersAreNotProcessing() {
                    final Set<IOrder> notProcessingOrders =
                            position.notProcessingOrders(order -> true);
                    assertTrue(notProcessingOrders.contains(buyOrder));
                    assertTrue(notProcessingOrders.contains(sellOrder));
                }

                @Test
                public void testFilledOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrders = position.filledOrders();
                    assertTrue(filledOrders.contains(buyOrder));
                    assertTrue(filledOrders.contains(sellOrder));
                }

                @Test
                public void testFilledOrOpenedOrdersHasBuyAndSellOrder() {
                    final Set<IOrder> filledOrOpenedOrders = position.filledOrOpenedOrders();
                    assertTrue(filledOrOpenedOrders.contains(buyOrder));
                    assertTrue(filledOrOpenedOrders.contains(sellOrder));
                }

                public class MarkingOrdersActive {

                    @Before
                    public void setUp() {
                        position.markAllOrdersActive();
                    }

                    @Test
                    public void testPositionHasBuyAndSellOrder() {
                        assertTrue(position.contains(buyOrder));
                        assertTrue(position.contains(sellOrder));
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
                        final double buyAmount = OrderStaticUtil.signedAmount(buyOrder);
                        final double sellAmount = OrderStaticUtil.signedAmount(sellOrder);
                        assertThat(position.signedExposure(), equalTo(buyAmount + sellAmount));
                    }

                    @Test
                    public void testOrdersHasBuyAndSellOrder() {
                        final Set<IOrder> orders = position.orders();
                        assertTrue(orders.contains(buyOrder));
                        assertTrue(orders.contains(sellOrder));
                    }

                    @Test
                    public void testOrdersAreProcessing() {
                        final Set<IOrder> notProcessingOrders =
                                position.notProcessingOrders(order -> true);
                        assertTrue(notProcessingOrders.isEmpty());
                    }

                    @Test
                    public void testFilledOrdersIsEmptySinceAllActive() {
                        final Set<IOrder> filledOrders = position.filledOrders();
                        assertTrue(filledOrders.isEmpty());
                    }

                    @Test
                    public void testFilledOrOpenedOrdersISEmptySinceAllActive() {
                        final Set<IOrder> filledOrOpenedOrders = position.filledOrOpenedOrders();
                        assertTrue(filledOrOpenedOrders.isEmpty());
                    }

                    @Test
                    public void testCloseOnTPRemovesOrderAlsoWhenMarkedActive() {
                        sendOrderEvent(sellOrder, OrderEventType.CLOSED_BY_TP);

                        assertFalse(position.contains(sellOrder));
                    }

                    @Test
                    public void testCloseOnSLRemovesOrderAlsoWhenMarkedActive() {
                        sendOrderEvent(sellOrder, OrderEventType.CLOSED_BY_SL);

                        assertFalse(position.contains(sellOrder));
                    }
                }

                private void assertBuyOrderRemoval(final OrderEventType orderEventType) {
                    sendOrderEvent(buyOrder, orderEventType);

                    assertFalse(position.contains(buyOrder));
                    assertThat(position.size(), equalTo(1));
                    assertThat(position.direction(), equalTo(OrderDirection.SHORT));

                    final double sellAmount = OrderStaticUtil.signedAmount(sellOrder);
                    assertThat(position.signedExposure(), equalTo(sellAmount));

                    final Set<IOrder> orders = position.orders();
                    assertTrue(orders.contains(sellOrder));

                    final Set<IOrder> filledOrders = position.filledOrders();
                    assertTrue(filledOrders.contains(sellOrder));

                    final Set<IOrder> filledOrOpenedOrders = position.filledOrOpenedOrders();
                    assertTrue(filledOrOpenedOrders.contains(sellOrder));
                }

                public class RemovingEvents {

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
            }
        }
    }
}