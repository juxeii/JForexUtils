package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class PositionChangeTask {

    private final PositionFactory positionFactory;
    private final OrderChangeUtil orderChangeUtil;
    private final PositionRetryLogic positionRetryLogic;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public PositionChangeTask(final PositionFactory positionFactory,
                              final OrderChangeUtil orderChangeUtil,
                              final PositionRetryLogic positionRetryLogic) {
        this.positionFactory = positionFactory;
        this.orderChangeUtil = orderChangeUtil;
        this.positionRetryLogic = positionRetryLogic;
    }

    public Completable closePosition(final Instrument instrument) {
        logger.debug("Starting to close " + instrument + " position");

        final Position position = positionFactory.forInstrument(instrument);
        final ConnectableObservable<OrderEvent> closeObservable =
                Observable.just(position.filledOrOpenedOrders())
                        .filter(ordersToClose -> !ordersToClose.isEmpty())
                        .doOnNext(ordersToClose -> position.markAllOrdersActive())
                        .flatMap(Observable::from)
                        .filter(order -> !isClosed.test(order))
                        .doOnNext(orderToClose -> logger.debug("Starting to close order " + orderToClose.getLabel()
                                + " for " + orderToClose.getInstrument() + " position."))
                        .flatMap(orderChangeUtil::close)
                        .retryWhen(positionRetryLogic::shouldRetry)
                        .doOnNext(orderEvent -> logger.debug("Order " + orderEvent.order().getLabel() + " closed for "
                                + orderEvent.order().getInstrument() + " position."))
                        .doOnCompleted(() -> logger.debug("Closing position " + instrument + " was successful."))
                        .doOnError(e -> logger.error("Closing position " + instrument + " failed!"))
                        .replay();
        closeObservable.connect();

        return closeObservable.toCompletable();
    }

    public Completable setSLCompletable(final IOrder orderToChangeSL,
                                        final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .flatMap(order -> orderChangeUtil.setStopLossPrice(order, newSL))
                .retryWhen(positionRetryLogic::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed SL from " + currentSL + " to " + newSL +
                        " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .toCompletable();
    }

    public Completable setTPCompletable(final IOrder orderToChangeTP,
                                        final double newTP) {
        final double currentTP = orderToChangeTP.getTakeProfitPrice();
        return Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(orderToChangeTP))
                .doOnNext(order -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + order.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .flatMap(order -> orderChangeUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(positionRetryLogic::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .toCompletable();
    }

    public Completable removeTPSLObs(final Set<IOrder> filledOrders) {
        final Completable removeTPObs = Observable.from(filledOrders)
                .doOnNext(order -> logger.debug("Remove TP from " + order.getLabel()))
                .flatMap(order -> setTPCompletable(order, platformSettings.noTPPrice()).toObservable())
                .toCompletable();
        final Completable removeSLObs = Observable.from(filledOrders)
                .doOnNext(order -> logger.debug("Remove SL from " + order.getLabel()))
                .flatMap(order -> setSLCompletable(order, platformSettings.noSLPrice()).toObservable())
                .toCompletable();
        return removeTPObs.concatWith(removeSLObs);
    }

    public Completable restoreSLTPObs(final IOrder mergedOrder,
                                      final RestoreSLTPData restoreSLTPData) {
        final Completable restoreSLObs = Observable.just(mergedOrder)
                .doOnNext(order -> logger.debug("Restore SL from " + order.getLabel()))
                .flatMap(order -> setSLCompletable(order, restoreSLTPData.sl()).toObservable())
                .toCompletable();
        final Completable restoreTPObs = Observable.just(mergedOrder)
                .doOnNext(order -> logger.debug("Restore TP from " + order.getLabel()))
                .flatMap(order -> setTPCompletable(order, restoreSLTPData.tp()).toObservable())
                .toCompletable();
        return restoreSLObs.concatWith(restoreTPObs);
    }
}
