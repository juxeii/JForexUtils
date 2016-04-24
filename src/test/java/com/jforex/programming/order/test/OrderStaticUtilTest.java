package com.jforex.programming.order.test;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.order.OrderStaticUtil.buyOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.combinedDirection;
import static com.jforex.programming.order.OrderStaticUtil.combinedSignedAmount;
import static com.jforex.programming.order.OrderStaticUtil.direction;
import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isNoSLSet;
import static com.jforex.programming.order.OrderStaticUtil.isNoTPSet;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.OrderStaticUtil.offerSideForOrderCommand;
import static com.jforex.programming.order.OrderStaticUtil.orderSLPredicate;
import static com.jforex.programming.order.OrderStaticUtil.orderTPPredicate;
import static com.jforex.programming.order.OrderStaticUtil.sellOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.signedAmount;
import static com.jforex.programming.order.OrderStaticUtil.statePredicate;
import static com.jforex.programming.order.OrderStaticUtil.switchCommand;
import static com.jforex.programming.order.OrderStaticUtil.switchDirection;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

public class OrderStaticUtilTest extends InstrumentUtilForTest {

    private final IOrderForTest firstOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest secondOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private Set<IOrder> orders;

    @Before
    public void setUp() throws JFException {
        firstOrderEURUSD.setState(IOrder.State.FILLED);
        secondOrderEURUSD.setState(IOrder.State.FILLED);
        secondOrderEURUSD.setLabel("SecondOrderLabel");

        orders = createSet(firstOrderEURUSD, secondOrderEURUSD);
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

        firstOrderEURUSD.setState(IOrder.State.CANCELED);
        assertTrue(orderCancelPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setState(IOrder.State.OPENED);
        assertFalse(orderCancelPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testOpenPredicateIsCorrect() {
        firstOrderEURUSD.setState(IOrder.State.OPENED);
        assertTrue(isOpened.test(firstOrderEURUSD));

        firstOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isOpened.test(firstOrderEURUSD));
    }

    @Test
    public void testFilledPredicateIsCorrect() {
        firstOrderEURUSD.setState(IOrder.State.FILLED);
        assertTrue(isFilled.test(firstOrderEURUSD));

        firstOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isFilled.test(firstOrderEURUSD));
    }

    @Test
    public void testClosedPredicateIsCorrect() {
        firstOrderEURUSD.setState(IOrder.State.CLOSED);
        assertTrue(isClosed.test(firstOrderEURUSD));

        firstOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isClosed.test(firstOrderEURUSD));
    }

    @Test
    public void testConditionalPredicateIsCorrect() {
        firstOrderEURUSD.setState(IOrder.State.OPENED);
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUYLIMIT);
        assertTrue(isConditional.test(firstOrderEURUSD));

        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        assertFalse(isConditional.test(firstOrderEURUSD));
    }

    @Test
    public void testSLPredicateIsCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = orderSLPredicate.apply(sl);

        firstOrderEURUSD.setStopLossPrice(sl);
        assertTrue(slPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setStopLossPrice(sl + 0.1);
        assertFalse(slPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testTPPredicateIsCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = orderTPPredicate.apply(tp);

        firstOrderEURUSD.setTakeProfitPrice(tp);
        assertTrue(tpPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setTakeProfitPrice(tp + 0.1);
        assertFalse(tpPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testIsSLSetToPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isSLSetTo(sl);

        firstOrderEURUSD.setStopLossPrice(sl);
        assertTrue(slPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setStopLossPrice(sl + 0.1);
        assertFalse(slPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testIsTPSetToPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isTPSetTo(tp);

        firstOrderEURUSD.setTakeProfitPrice(tp);
        assertTrue(tpPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setTakeProfitPrice(tp + 0.1);
        assertFalse(tpPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testIsNoSLSetPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isNoSLSet;

        firstOrderEURUSD.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());
        assertTrue(slPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setStopLossPrice(sl);
        assertFalse(slPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testIsNoTPSetPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isNoTPSet;

        firstOrderEURUSD.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());
        assertTrue(tpPredicate.test(firstOrderEURUSD));

        firstOrderEURUSD.setTakeProfitPrice(tp);
        assertFalse(tpPredicate.test(firstOrderEURUSD));
    }

    @Test
    public void testOrderDirectionIsFlatWhenOrderIsNull() {
        assertThat(direction(null), equalTo(OrderDirection.FLAT));
    }

    @Test
    public void testOrderDirectionIsLongForBuyCommand() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(direction(firstOrderEURUSD), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testOrderDirectionIsShortForSellCommand() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(direction(firstOrderEURUSD), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveBuyCommands() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveSellCommands() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsFlatForBothOrdersCancelOut() {
        final double amount = 0.12;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(amount);
        secondOrderEURUSD.setAmount(amount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.FLAT));
    }

    @Test
    public void testCombinedDirectionIsLongWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(buyAmount);
        secondOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsShortWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(buyAmount);
        secondOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testSignedAmountIsPositiveForBuyCommand() {
        final double buyAmount = 0.11;
        firstOrderEURUSD.setAmount(buyAmount);
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(signedAmount(firstOrderEURUSD), equalTo(buyAmount));
        assertThat(signedAmount(buyAmount, OrderCommand.BUY), equalTo(buyAmount));
    }

    @Test
    public void testSignedAmountIsNegativeForSellCommand() {
        final double sellAmount = 0.11;
        firstOrderEURUSD.setAmount(sellAmount);
        firstOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(signedAmount(firstOrderEURUSD), equalTo(-sellAmount));
        assertThat(signedAmount(sellAmount, OrderCommand.SELL), equalTo(-sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveForBothOrdersHaveBuyCommands() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(combinedSignedAmount(orders),
                   equalTo(firstOrderEURUSD.getAmount() + secondOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeForBothOrdersHaveSellCommands() {
        firstOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(combinedSignedAmount(orders),
                   equalTo(-firstOrderEURUSD.getAmount() - secondOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsZeroForBothOrdersCancelOut() {
        final double amount = 0.12;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(amount);
        secondOrderEURUSD.setAmount(amount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(0.0));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(buyAmount);
        secondOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyAmount - sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        firstOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        secondOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        firstOrderEURUSD.setAmount(buyAmount);
        secondOrderEURUSD.setAmount(sellAmount);

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
    public void testDirectionToCommand() {
        assertThat(directionToCommand(OrderDirection.LONG), equalTo(OrderCommand.BUY));
        assertThat(directionToCommand(OrderDirection.SHORT), equalTo(OrderCommand.SELL));
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
    public void testSwitchOrderDirectionIsCorrect() {
        assertThat(switchDirection(OrderDirection.FLAT), equalTo(OrderDirection.FLAT));
        assertThat(switchDirection(OrderDirection.LONG), equalTo(OrderDirection.SHORT));
        assertThat(switchDirection(OrderDirection.SHORT), equalTo(OrderDirection.LONG));
    }
}
