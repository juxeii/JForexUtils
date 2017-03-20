package com.jforex.programming.order.test;

import static com.jforex.programming.order.OrderStaticUtil.adaptedOrderParamsForSignedAmount;
import static com.jforex.programming.order.OrderStaticUtil.amountPredicate;
import static com.jforex.programming.order.OrderStaticUtil.buyOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.combinedSignedAmount;
import static com.jforex.programming.order.OrderStaticUtil.direction;
import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;
import static com.jforex.programming.order.OrderStaticUtil.gttPredicate;
import static com.jforex.programming.order.OrderStaticUtil.instrumentPredicate;
import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isCanceled;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isNoSLSet;
import static com.jforex.programming.order.OrderStaticUtil.isNoTPSet;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.OrderStaticUtil.labelPredicate;
import static com.jforex.programming.order.OrderStaticUtil.ofInstrument;
import static com.jforex.programming.order.OrderStaticUtil.offerSideForOrderCommand;
import static com.jforex.programming.order.OrderStaticUtil.openPricePredicate;
import static com.jforex.programming.order.OrderStaticUtil.sellOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.signedAmount;
import static com.jforex.programming.order.OrderStaticUtil.signedAmountForReplace;
import static com.jforex.programming.order.OrderStaticUtil.slPredicate;
import static com.jforex.programming.order.OrderStaticUtil.statePredicate;
import static com.jforex.programming.order.OrderStaticUtil.switchCommand;
import static com.jforex.programming.order.OrderStaticUtil.switchDirection;
import static com.jforex.programming.order.OrderStaticUtil.tpPredicate;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.position.PositionDirection;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderStaticUtilTest extends InstrumentUtilForTest {

    private final Set<IOrder> orders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() throws JFException {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
        orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.FILLED);
        orderUtilForTest.setLabel(sellOrderEURUSD, "SecondOrderLabel");
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(OrderStaticUtil.class);
    }

    @Test
    public void testBuyOrderCommandSet() {
        assertTrue(buyOrderCommands.contains(OrderCommand.BUY));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYLIMIT));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYLIMIT_BYBID));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYSTOP));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYSTOP_BYBID));
    }

    @Test
    public void testSellOrderCommandSet() {
        assertTrue(sellOrderCommands.contains(OrderCommand.SELL));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLLIMIT));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLLIMIT_BYASK));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLSTOP));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLSTOP_BYASK));
    }

    @Test
    public void testStatePredicateIsCorrect() {
        final Predicate<IOrder> orderCancelPredicate = statePredicate.apply(IOrder.State.CANCELED);

        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);
        assertTrue(orderCancelPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.OPENED);
        assertFalse(orderCancelPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testOpenPredicateIsCorrect() {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.OPENED);
        assertTrue(isOpened.test(buyOrderEURUSD));

        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);
        assertFalse(isOpened.test(buyOrderEURUSD));
    }

    @Test
    public void testFilledPredicateIsCorrect() {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
        assertTrue(isFilled.test(buyOrderEURUSD));

        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);
        assertFalse(isFilled.test(buyOrderEURUSD));
    }

    @Test
    public void testClosedPredicateIsCorrect() {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CLOSED);
        assertTrue(isClosed.test(buyOrderEURUSD));

        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);
        assertFalse(isClosed.test(buyOrderEURUSD));
    }

    @Test
    public void testCanceledPredicateIsCorrect() {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.CANCELED);
        assertTrue(isCanceled.test(buyOrderEURUSD));
    }

    @Test
    public void testConditionalPredicateIsCorrect() {
        orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.OPENED);
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUYLIMIT);
        assertTrue(isConditional.test(buyOrderEURUSD));

        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        assertFalse(isConditional.test(buyOrderEURUSD));
    }

    @Test
    public void testLabelPredicateIsCorrect() throws JFException {
        final String label = "label";
        final Predicate<IOrder> predicate = labelPredicate.apply(label);

        orderUtilForTest.setLabel(buyOrderEURUSD, label);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setLabel(buyOrderEURUSD, "wrong" + label);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testGTTPredicateIsCorrect() throws JFException {
        final long gtt = 1234L;
        final Predicate<IOrder> predicate = gttPredicate.apply(gtt);

        orderUtilForTest.setGTT(buyOrderEURUSD, gtt);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setGTT(buyOrderEURUSD, gtt + 1L);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testAmountPredicateIsCorrect() throws JFException {
        final double amount = 1.34521;
        final Predicate<IOrder> predicate = amountPredicate.apply(amount);

        orderUtilForTest.setRequestedAmount(buyOrderEURUSD, amount);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setRequestedAmount(buyOrderEURUSD, amount + 0.1);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testOpenPricePredicateIsCorrect() throws JFException {
        final double openPrice = 1.34521;
        final Predicate<IOrder> predicate = openPricePredicate.apply(openPrice);

        orderUtilForTest.setOpenPrice(buyOrderEURUSD, openPrice);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setOpenPrice(buyOrderEURUSD, openPrice + 0.1);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testSLPredicateIsCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> predicate = slPredicate.apply(sl);

        orderUtilForTest.setSL(buyOrderEURUSD, sl);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setSL(buyOrderEURUSD, sl + 0.1);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testTPPredicateIsCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> predicate = tpPredicate.apply(tp);

        orderUtilForTest.setTP(buyOrderEURUSD, tp);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setTP(buyOrderEURUSD, tp + 0.1);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsLabelSetToPredicateCorrect() throws JFException {
        final String label = "label";
        final Predicate<IOrder> slPredicate = isLabelSetTo(label);

        orderUtilForTest.setLabel(buyOrderEURUSD, label);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setLabel(buyOrderEURUSD, "other" + label);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsGTTSetToPredicateCorrect() throws JFException {
        final long gtt = 1234L;
        final Predicate<IOrder> slPredicate = isGTTSetTo(gtt);

        orderUtilForTest.setGTT(buyOrderEURUSD, gtt);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setGTT(buyOrderEURUSD, gtt + 1L);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsOpenPriceSetToPredicateCorrect() throws JFException {
        final double openPrice = 1.1234;
        final Predicate<IOrder> slPredicate = isOpenPriceSetTo(openPrice);

        orderUtilForTest.setOpenPrice(buyOrderEURUSD, openPrice);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setOpenPrice(buyOrderEURUSD, openPrice + 0.1);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsAmountSetToPredicateCorrect() throws JFException {
        final double amount = 0.12;
        final Predicate<IOrder> predicate = isAmountSetTo(amount);

        orderUtilForTest.setRequestedAmount(buyOrderEURUSD, amount);
        assertTrue(predicate.test(buyOrderEURUSD));

        orderUtilForTest.setRequestedAmount(buyOrderEURUSD, amount + 0.1);
        assertFalse(predicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsSLSetToPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isSLSetTo(sl);

        orderUtilForTest.setSL(buyOrderEURUSD, sl);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setSL(buyOrderEURUSD, sl + 0.1);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsTPSetToPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isTPSetTo(tp);

        orderUtilForTest.setTP(buyOrderEURUSD, tp);
        assertTrue(tpPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setTP(buyOrderEURUSD, tp + 0.1);
        assertFalse(tpPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsNoSLSetPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isNoSLSet;

        orderUtilForTest.setSL(buyOrderEURUSD, platformSettings.noSLPrice());
        assertTrue(slPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setSL(buyOrderEURUSD, sl);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsNoTPSetPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isNoTPSet;

        orderUtilForTest.setTP(buyOrderEURUSD, platformSettings.noTPPrice());
        assertTrue(tpPredicate.test(buyOrderEURUSD));

        orderUtilForTest.setTP(buyOrderEURUSD, tp);
        assertFalse(tpPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testInstrumentPredicateIsCorrect() throws JFException {
        final Predicate<IOrder> predicate = instrumentPredicate.apply(instrumentEURUSD);

        assertTrue(predicate.test(buyOrderEURUSD));
        assertFalse(predicate.test(orderUtilForTest.sellOrderAUDUSD()));

        assertTrue(ofInstrument(instrumentEURUSD).test(buyOrderEURUSD));
        assertFalse(ofInstrument(instrumentAUDUSD).test(buyOrderEURUSD));
    }

    @Test
    public void testOrderDirectionIsLongForBuyCommand() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);

        assertThat(direction(buyOrderEURUSD), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testOrderDirectionIsShortForSellCommand() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.SELL);

        assertThat(direction(buyOrderEURUSD), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveBuyCommands() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.BUY);

        assertThat(PositionUtil.direction(orders), equalTo(PositionDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveSellCommands() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);

        assertThat(PositionUtil.direction(orders), equalTo(PositionDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsFlatForBothOrdersCancelOut() {
        final double amount = 0.12;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, amount);
        orderUtilForTest.setAmount(sellOrderEURUSD, amount);

        assertThat(PositionUtil.direction(orders), equalTo(PositionDirection.FLAT));
    }

    @Test
    public void testCombinedDirectionIsLongWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, buyAmount);
        orderUtilForTest.setAmount(sellOrderEURUSD, sellAmount);

        assertThat(PositionUtil.direction(orders), equalTo(PositionDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsShortWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, buyAmount);
        orderUtilForTest.setAmount(sellOrderEURUSD, sellAmount);

        assertThat(PositionUtil.direction(orders), equalTo(PositionDirection.SHORT));
    }

    @Test
    public void testSignedAmountIsPositiveForBuyCommand() {
        final double buyAmount = 0.11;
        orderUtilForTest.setAmount(buyOrderEURUSD, buyAmount);
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);

        assertThat(signedAmount(buyOrderEURUSD), equalTo(buyAmount));
        assertThat(signedAmount(buyAmount, OrderCommand.BUY), equalTo(buyAmount));
    }

    @Test
    public void testSignedAmountForPositiveParamsAmount() {
        assertThat(signedAmount(buyParamsEURUSD), equalTo(buyParamsEURUSD.amount()));
    }

    @Test
    public void testSignedAmountForNegativeParamsAmount() {
        assertThat(signedAmount(sellParamsEURUSD), equalTo(-sellParamsEURUSD.amount()));
    }

    @Test
    public void testSignedAmountIsNegativeForSellCommand() {
        final double sellAmount = 0.11;
        orderUtilForTest.setAmount(buyOrderEURUSD, sellAmount);
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.SELL);

        assertThat(signedAmount(buyOrderEURUSD), equalTo(-sellAmount));
        assertThat(signedAmount(sellAmount, OrderCommand.SELL), equalTo(-sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveForBothOrdersHaveBuyCommands() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.BUY);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyOrderEURUSD.getAmount() + sellOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeForBothOrdersHaveSellCommands() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);

        assertThat(combinedSignedAmount(orders),
                   equalTo(-buyOrderEURUSD.getAmount() - sellOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsZeroForBothOrdersCancelOut() {
        final double amount = 0.12;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, amount);
        orderUtilForTest.setAmount(sellOrderEURUSD, amount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(0.0));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, buyAmount);
        orderUtilForTest.setAmount(sellOrderEURUSD, sellAmount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyAmount - sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);
        orderUtilForTest.setOrderCommand(sellOrderEURUSD, OrderCommand.SELL);
        orderUtilForTest.setAmount(buyOrderEURUSD, buyAmount);
        orderUtilForTest.setAmount(sellOrderEURUSD, sellAmount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyAmount - sellAmount));
    }

    @Test
    public void testOfferSideForOrderCommandIsAskForBuyCommand() {
        assertThat(offerSideForOrderCommand(OrderCommand.BUY), equalTo(OfferSide.ASK));
    }

    @Test
    public void testOfferSideForOrderCommandIsBidForSellCommand() {
        assertThat(offerSideForOrderCommand(OrderCommand.SELL), equalTo(OfferSide.BID));
    }

    @Test
    public void directionToCommandIsCorrect() {
        assertThat(directionToCommand(OrderDirection.LONG), equalTo(OrderCommand.BUY));
        assertThat(directionToCommand(OrderDirection.SHORT), equalTo(OrderCommand.SELL));
    }

    @Test
    public void switchDirectionIsCorrect() {
        assertThat(switchDirection(OrderDirection.LONG), equalTo(OrderDirection.SHORT));
        assertThat(switchDirection(OrderDirection.SHORT), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testSwitchOrderCommandIsCorrect() {
        assertThat(switchCommand(OrderCommand.BUY), equalTo(OrderCommand.SELL));
        assertThat(switchCommand(OrderCommand.SELL), equalTo(OrderCommand.BUY));

        assertThat(switchCommand(OrderCommand.BUYLIMIT), equalTo(OrderCommand.SELLLIMIT));
        assertThat(switchCommand(OrderCommand.SELLLIMIT), equalTo(OrderCommand.BUYLIMIT));

        assertThat(switchCommand(OrderCommand.BUYLIMIT_BYBID), equalTo(OrderCommand.SELLLIMIT_BYASK));
        assertThat(switchCommand(OrderCommand.SELLLIMIT_BYASK), equalTo(OrderCommand.BUYLIMIT_BYBID));

        assertThat(switchCommand(OrderCommand.BUYSTOP), equalTo(OrderCommand.SELLSTOP));
        assertThat(switchCommand(OrderCommand.SELLSTOP), equalTo(OrderCommand.BUYSTOP));

        assertThat(switchCommand(OrderCommand.BUYSTOP_BYBID), equalTo(OrderCommand.SELLSTOP_BYASK));
        assertThat(switchCommand(OrderCommand.SELLSTOP_BYASK), equalTo(OrderCommand.BUYSTOP_BYBID));
    }

    @Test
    public void runnableToCallableIsCorrect() throws Exception {
        final Callable<IOrder> callable = () -> {
            buyOrderEURUSD.getLabel();
            return buyOrderEURUSD;
        };
        final IOrder order = callable.call();

        assertThat(order, equalTo(buyOrderEURUSD));
        verify(buyOrderEURUSD).getLabel();
    }

    public class AdaptedOrderParams {

        private OrderParams adaptedOrderParams;

        private void assertAdaptedOrderParams(final OrderCommand orderCommand,
                                              final double amount) {
            assertThat(adaptedOrderParams.orderCommand(),
                       equalTo(orderCommand));
            assertThat(adaptedOrderParams.amount(),
                       closeTo(amount, 0.0001));
        }

        @Test
        public void adaptedOrderParamsForPositiveAmountIsCorrect() {
            adaptedOrderParams = adaptedOrderParamsForSignedAmount(buyParamsEURUSD, 0.12);

            assertAdaptedOrderParams(OrderCommand.BUY, 0.12);
        }

        @Test
        public void adaptedOrderParamsForNegativeAmountIsCorrect() {
            adaptedOrderParams = adaptedOrderParamsForSignedAmount(buyParamsEURUSD, -0.12);

            assertAdaptedOrderParams(OrderCommand.SELL, 0.12);
        }
    }

    @Test
    public void signedAmountForReplaceIsCorrectWhenCurrentAmountIsZero() {
        assertThat(signedAmountForReplace(0.0, -0.12), equalTo(-0.12));
        assertThat(signedAmountForReplace(0.0, 0.0), equalTo(0.0));
        assertThat(signedAmountForReplace(0.0, 0.04123), equalTo(0.04123));
    }

    @Test
    public void signedAmountForReplaceIsCorrectWhenCurrentAmountIsPositive() {
        assertThat(signedAmountForReplace(0.023, 0.12), equalTo(0.097));
        assertThat(signedAmountForReplace(0.044, 0.012), equalTo(-0.032));

        assertThat(signedAmountForReplace(0.023, -0.12), equalTo(-0.143));
        assertThat(signedAmountForReplace(0.044, -0.012), equalTo(-0.056));
    }

    @Test
    public void signedAmountForReplaceIsCorrectWhenCurrentAmountIsNegative() {
        assertThat(signedAmountForReplace(-0.023, 0.12), equalTo(0.143));
        assertThat(signedAmountForReplace(-0.044, 0.012), equalTo(0.056));

        assertThat(signedAmountForReplace(-0.023, -0.12), equalTo(-0.097));
        assertThat(signedAmountForReplace(-0.044, -0.012), equalTo(0.032));
    }

    @Test
    public void signedAmountForReplaceOrderIsCorrectWhenCurrentAmountIsPositive() {
        orderUtilForTest.setAmount(buyOrderEURUSD, 0.023);
        assertThat(signedAmountForReplace(buyOrderEURUSD, 0.12), equalTo(0.097));
        assertThat(signedAmountForReplace(buyOrderEURUSD, -0.12), equalTo(-0.143));

        orderUtilForTest.setAmount(buyOrderEURUSD, 0.044);
        assertThat(signedAmountForReplace(buyOrderEURUSD, 0.012), equalTo(-0.032));
        assertThat(signedAmountForReplace(buyOrderEURUSD, -0.012), equalTo(-0.056));
    }

    @Test
    public void signedAmountForReplaceOrderIsCorrectWhenCurrentAmountIsNegative() {
        orderUtilForTest.setAmount(sellOrderEURUSD, 0.023);
        assertThat(signedAmountForReplace(sellOrderEURUSD, 0.12), equalTo(0.143));
        assertThat(signedAmountForReplace(sellOrderEURUSD, -0.12), equalTo(-0.097));

        orderUtilForTest.setAmount(sellOrderEURUSD, 0.044);
        assertThat(signedAmountForReplace(sellOrderEURUSD, 0.012), equalTo(0.056));
        assertThat(signedAmountForReplace(sellOrderEURUSD, -0.012), equalTo(0.032));
    }
}
