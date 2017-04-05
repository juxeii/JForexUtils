package com.jforex.programming.order.task;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.OrderParams;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class TaskExecutor {

    private final StrategyThreadRunner strategyThreadRunner;
    private final IEngine engine;

    private final static Logger logger = LogManager.getLogger(TaskExecutor.class);

    public TaskExecutor(final StrategyThreadRunner strategyThreadRunner,
                        final IEngine engine) {
        this.strategyThreadRunner = strategyThreadRunner;
        this.engine = engine;
    }

    public Single<IOrder> submitOrder(final OrderParams orderParams) {
        return single(() -> engine.submitOrder(orderParams.label(),
                                               orderParams.instrument(),
                                               orderParams.orderCommand(),
                                               orderParams.amount(),
                                               orderParams.price(),
                                               orderParams.slippage(),
                                               orderParams.stopLossPrice(),
                                               orderParams.takeProfitPrice(),
                                               orderParams.goodTillTime(),
                                               orderParams.comment()));
    }

    public Single<IOrder> mergeOrders(final String mergeOrderLabel,
                                      final Collection<IOrder> toMergeOrders) {
        return single(() -> engine.mergeOrders(mergeOrderLabel, toMergeOrders));
    }

    public Completable close(final IOrder order,
                             final double amount) {
        logger.info("Called normal close");
        return completable(() -> order.close(amount));
    }

    public Completable close(final IOrder order,
                             final double amount,
                             final double price,
                             final double slippage) {
        logger.info("Called complex close");
        return completable(() -> order.close(amount,
                                             price,
                                             slippage));
    }

    public Completable setLabel(final IOrder order,
                                final String label) {
        return completable(() -> order.setLabel(label));
    }

    public Completable setGoodTillTime(final IOrder order,
                                       final long newGTT) {
        return completable(() -> order.setGoodTillTime(newGTT));
    }

    public Completable setRequestedAmount(final IOrder order,
                                          final double newRequestedAmount) {
        return completable(() -> order.setRequestedAmount(newRequestedAmount));
    }

    public Completable setOpenPrice(final IOrder order,
                                    final double newOpenPrice) {
        return completable(() -> order.setOpenPrice(newOpenPrice));
    }

    public Completable setStopLossPrice(final IOrder order,
                                        final double newSL,
                                        final OfferSide offerSide,
                                        final double trailingStep) {
        return completable(() -> order.setStopLossPrice(newSL,
                                                        offerSide,
                                                        trailingStep));
    }

    public Completable setTakeProfitPrice(final IOrder order,
                                          final double newTP) {
        return completable(() -> order.setTakeProfitPrice(newTP));
    }

    private Single<IOrder> single(final Callable<IOrder> callable) {
        return strategyThreadRunner.execute(callable);
    }

    private Completable completable(final Action action) {
        return strategyThreadRunner.execute(action);
    }
}
