package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.settings.PlatformSettings;

import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final PositionFactory positionFactory;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(OrderUtil.class);

    public OrderUtil(final IEngine engine,
                     final PositionFactory positionFactory,
                     final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.positionFactory = positionFactory;
        this.orderUtilHandler = orderUtilHandler;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }

    private Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    private Instrument instrumentFromOrders(final Collection<IOrder> orders) {
        return orders
            .iterator()
            .next()
            .getInstrument();
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();

        return orderUtilObservable(new SubmitCommand(orderParams, engine))
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnNext(this::addCreatedOrderToPosition)
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."));
    }

    private void addCreatedOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public Observable<OrderEvent> submitAndMergePosition(final String mergeOrderLabel,
                                                         final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        return submitOrder(orderParams)
            .concatWith(Observable.defer(() -> mergePositionOrders(mergeOrderLabel, orderParams.instrument())));
    }

    public Observable<OrderEvent> submitAndMergePositionToParams(final String mergeOrderLabel,
                                                                 final OrderParams orderParams) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(orderParams);

        final double signedPositionAmount = position(orderParams.instrument()).signedExposure();
        final double signedParamsAmount = OrderStaticUtil.signedAmount(orderParams);
        final double signedNeededAmount = signedParamsAmount - signedPositionAmount;
        final OrderParams adaptedOrderParams =
                OrderStaticUtil.adaptedOrderParamsForSignedAmount(orderParams, signedNeededAmount);

        return submitAndMergePosition(mergeOrderLabel, adaptedOrderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> {
                        logger.info("Starting to merge with label " + mergeOrderLabel
                                + " for position " + instrumentFromOrders(toMergeOrders) + ".");
                        position(toMergeOrders).markOrdersActive(toMergeOrders);
                    })
                    .flatMap(this::removeTPSLObservable)
                    .toCompletable()
                    .andThen(orderUtilObservable(new MergeCommand(mergeOrderLabel, toMergeOrders, engine)))
                    .doOnNext(this::addCreatedOrderToPosition)
                    .doOnTerminate(() -> position(toMergeOrders).markOrdersIdle(toMergeOrders))
                    .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                    .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(instrument);

        return mergeOrders(mergeOrderLabel, position(instrument).filled())
            .doOnSubscribe(() -> logger.info("Starting position merge for " +
                    instrument + " with label " + mergeOrderLabel))
            .doOnError(e -> logger.error("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " failed!" + "Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Position merge for " + instrument
                    + "  with label " + mergeOrderLabel + " was successful."));
    }

    private Observable<OrderEvent> removeTPSLObservable(final Collection<IOrder> filledOrders) {
        final Instrument instrument = instrumentFromOrders(filledOrders);

        return Observable
            .from(filledOrders)
            .doOnSubscribe(() -> logger.info("Starting remove TPSL task for position " + instrument))
            .flatMap(this::removeSingleTPSLObservable)
            .doOnError(e -> logger.error("Removing TPSL from " + instrument + " failed! Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Removing TPSL task from " + instrument + " was successful."));
    }

    private final Observable<OrderEvent> removeSingleTPSLObservable(final IOrder orderToRemoveSLTP) {
        return setTakeProfitPrice(orderToRemoveSLTP, platformSettings.noSLPrice())
            .concatWith(setStopLossPrice(orderToRemoveSLTP, platformSettings.noTPPrice()));
    }

    public Observable<OrderEvent> closePosition(final Instrument instrument) {
        final Position position = position(checkNotNull(instrument));
        final Set<IOrder> ordersToClose = position.filledOrOpened();

        return ordersToClose.isEmpty()
                ? Observable.empty()
                : Observable
                    .from(ordersToClose)
                    .doOnSubscribe(() -> {
                        logger.info("Starting position close for " + instrument);
                        position.markOrdersActive(ordersToClose);
                    })
                    .flatMap(this::close)
                    .doOnTerminate(() -> position.markOrdersIdle(ordersToClose))
                    .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
                    .doOnError(e -> logger.error("Closing position " + instrument
                            + " failed! Exception: " + e.getMessage()));
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        final OrderChangeCommand<?> command =
                new CloseCommand(checkNotNull(orderToClose));
        final String commonLog = "state" + " from " + orderToClose.getState() + " to "
                + IOrder.State.CLOSED + " for order " + orderToClose.getLabel() + " and instrument "
                + orderToClose.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        final OrderChangeCommand<?> command =
                new SetLabelCommand(checkNotNull(orderToChangeLabel), newLabel);
        final String commonLog = "label" + " from " + orderToChangeLabel.getLabel() + " to "
                + newLabel + " for order " + orderToChangeLabel.getLabel() + " and instrument "
                + orderToChangeLabel.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        final OrderChangeCommand<?> command =
                new SetGTTCommand(checkNotNull(orderToChangeGTT), newGTT);
        final String commonLog = "GTT" + " from " + orderToChangeGTT.getGoodTillTime() + " to "
                + newGTT + " for order " + orderToChangeGTT.getLabel() + " and instrument "
                + orderToChangeGTT.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        final OrderChangeCommand<?> command =
                new SetOpenPriceCommand(checkNotNull(orderToChangeOpenPrice), newOpenPrice);
        final String commonLog = "open price" + " from " + orderToChangeOpenPrice.getOpenPrice() + " to "
                + newOpenPrice + " for order " + orderToChangeOpenPrice.getLabel() + " and instrument "
                + orderToChangeOpenPrice.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        final OrderChangeCommand<?> command =
                new SetAmountCommand(checkNotNull(orderToChangeAmount), newRequestedAmount);
        final String commonLog = "amount" + " from " + orderToChangeAmount.getRequestedAmount() + " to "
                + newRequestedAmount + " for order " + orderToChangeAmount.getLabel() + " and instrument "
                + orderToChangeAmount.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        final OrderChangeCommand<?> command =
                new SetSLCommand(checkNotNull(orderToChangeSL), newSL);
        final String commonLog = "SL" + " from " + orderToChangeSL.getStopLossPrice() + " to "
                + newSL + " for order " + orderToChangeSL.getLabel() + " and instrument "
                + orderToChangeSL.getInstrument();

        return changeObservable(command, commonLog);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        final OrderChangeCommand<?> command =
                new SetTPCommand(checkNotNull(orderToChangeTP), newTP);
        final String commonLog = "TP" + " from " + orderToChangeTP.getTakeProfitPrice() + " to "
                + newTP + " for order " + orderToChangeTP.getLabel() + " and instrument "
                + orderToChangeTP.getInstrument();

        return changeObservable(command, commonLog);
    }

    private Observable<OrderEvent> changeObservable(final OrderChangeCommand<?> command,
                                                    final String commonLog) {
        return Observable
            .just(command)
            .filter(OrderChangeCommand::isValueNotSet)
            .doOnSubscribe(() -> logger.info("Start to change " + commonLog))
            .flatMap(this::orderUtilObservable)
            .doOnError(e -> logger.error("Failed to change " + commonLog + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed " + commonLog));
    }

    private Observable<OrderEvent> orderUtilObservable(final OrderCallCommand command) {
        return orderUtilHandler.callObservable(command);
    }
}
