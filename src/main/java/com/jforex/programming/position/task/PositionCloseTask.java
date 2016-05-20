package com.jforex.programming.position.task;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionRetryLogic;

import rx.Completable;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class PositionCloseTask {

    private final PositionFactory positionFactory;
    private final OrderChangeUtil orderChangeUtil;
    private final PositionRetryLogic positionRetryLogic;

    private static final Logger logger = LogManager.getLogger(PositionCloseTask.class);

    public PositionCloseTask(final PositionFactory positionFactory,
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
}
