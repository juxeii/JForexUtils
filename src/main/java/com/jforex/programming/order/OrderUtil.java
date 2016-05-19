package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Completable;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class OrderUtil {

    private final OrderCreateUtil orderCreateUtil;
    private final OrderChangeUtil orderChangeUtil;
    private final PositionFactory positionFactory;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static int exceededRetryCount = platformSettings.maxRetriesOnOrderFail() + 1;
    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final OrderCreateUtil orderCreateUtil,
                     final OrderChangeUtil orderChangeUtil,
                     final PositionFactory positionFactory) {
        this.orderCreateUtil = orderCreateUtil;
        this.orderChangeUtil = orderChangeUtil;
        this.positionFactory = positionFactory;
    }

    public Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        logger.debug("Start submit task with label " + orderParams.label() + " for " + instrument + " position.");

        final Observable<OrderEvent> submitObs = orderCreateUtil.submitOrder(orderParams);
        final Position position = positionFactory.forInstrument(orderParams.instrument());
        submitObs.doOnCompleted(() -> logger.debug("Submit " + orderParams.label() + " for position "
                + instrument + " was successful."))
                .subscribe(orderEvent -> {
                    if (OrderEventTypeData.submitData.isDoneType(orderEvent.type()))
                        position.addOrder(orderEvent.order());
                },
                           e -> logger.error("Submit " + orderParams.label() + " for position "
                                   + instrument + " failed!"));
        return submitObs;
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final Position position = positionFactory.forInstrument(toMergeOrders.iterator().next().getInstrument());
        final Observable<OrderEvent> mergeObs = orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders);
        mergeObs.subscribe(orderEvent -> position.addOrder(orderEvent.order()),
                           e -> logger.error("Merge for " + mergeOrderLabel + " failed!"),
                           () -> logger.debug("Merge for " + mergeOrderLabel + " was successful."));

        return mergeObs;
    }

    public Completable mergePositionOrders(final String mergeOrderLabel,
                                           final Instrument instrument,
                                           final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Position position = positionFactory.forInstrument(instrument);
        final Set<IOrder> ordersToMerge = position.filledOrders();
        if (ordersToMerge.size() < 2) {
            logger.warn("Cannot merge " + instrument + " position with only " + ordersToMerge.size() + " orders!");
            return Completable.complete();
        }

        logger.debug("Starting merge task for " + instrument + " position with label " + mergeOrderLabel);
        position.markAllOrdersActive();
        final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSLTPPolicy.restoreSL(ordersToMerge),
                                                                    restoreSLTPPolicy.restoreTP(ordersToMerge));

        final Completable mergeSequence =
                removeTPSLObs(ordersToMerge)
                        .concatWith(Observable.defer(() -> {
                            logger.debug("Start merge with label " + mergeOrderLabel);
                            return mergeOrders(mergeOrderLabel, ordersToMerge)
                                    .retryWhen(this::shouldRetry)
                                    .flatMap(orderEvent -> Observable.just(orderEvent.order()));
                        })
                                .flatMap(mergeOrder -> restoreSLTPObs(mergeOrder, restoreSLTPData).toObservable())
                                .toCompletable());
        final ConnectableObservable<?> mergeObs = mergeSequence.toObservable().replay();
        mergeObs.connect();

        return mergeObs.toCompletable();
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
                        .flatMap(this::close)
                        .retryWhen(this::shouldRetry)
                        .doOnNext(orderEvent -> logger.debug("Order " + orderEvent.order().getLabel() + " closed for "
                                + orderEvent.order().getInstrument() + " position."))
                        .doOnCompleted(() -> logger.debug("Closing position " + instrument + " was successful."))
                        .doOnError(e -> logger.error("Closing position " + instrument + " failed!"))
                        .replay();
        closeObservable.connect();

        return closeObservable.toCompletable();
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final Observable<OrderEvent> closeObs = orderChangeUtil.close(orderToClose);
        closeObs.doOnCompleted(() -> logger.debug("Closing " + orderToClose.getLabel() + " was successful."))
                .subscribe(orderEvent -> {},
                           e -> logger.error("Closing " + orderToClose.getLabel() + " failed!"));
        return closeObs;
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderChangeUtil.setLabel(orderToChangeLabel, newLabel);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderChangeUtil.setGoodTillTime(orderToChangeGTT, newGTT);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderChangeUtil.setOpenPrice(orderToChangeOpenPrice, newOpenPrice);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return orderChangeUtil.setRequestedAmount(orderToChangeAmount, newRequestedAmount);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderChangeUtil.setStopLossPrice(orderToChangeSL, newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderChangeUtil.setTakeProfitPrice(orderToChangeTP, newTP);
    }

    private Completable setSLCompletable(final IOrder orderToChangeSL,
                                         final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .flatMap(order -> setStopLossPrice(order, newSL))
                .retryWhen(this::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed SL from " + currentSL + " to " + newSL +
                        " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .toCompletable();
    }

    private Completable setTPCompletable(final IOrder orderToChangeTP,
                                         final double newTP) {
        final double currentTP = orderToChangeTP.getTakeProfitPrice();
        return Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(orderToChangeTP))
                .doOnNext(order -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + order.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .flatMap(order -> setTakeProfitPrice(order, newTP))
                .retryWhen(this::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .toCompletable();
    }

    private Completable removeTPSLObs(final Set<IOrder> filledOrders) {
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

    private Completable restoreSLTPObs(final IOrder mergedOrder,
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

    private Observable<?> shouldRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(this::filterErrorType)
                .zipWith(Observable.range(1, exceededRetryCount), Pair::of)
                .flatMap(this::evaluateRetryPair);
    }

    private Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair) {
        return retryPair.getRight() == exceededRetryCount
                ? Observable.error(retryPair.getLeft())
                : Observable
                        .interval(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS)
                        .take(1);
    }

    private Observable<? extends Throwable> filterErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}
