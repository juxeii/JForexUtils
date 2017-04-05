package com.jforex.programming.order.task;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.basic.SetAmountParams;
import com.jforex.programming.order.task.params.basic.SetGTTParams;
import com.jforex.programming.order.task.params.basic.SetLabelParams;
import com.jforex.programming.order.task.params.basic.SetOpenPriceParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class BasicTask {

    private final TaskExecutor taskExecutor;
    private final OrderUtilHandler orderUtilHandler;
    private final CalculationUtil calculationUtil;

    private final static Logger logger = LogManager.getLogger(BasicTask.class);

    public BasicTask(final TaskExecutor taskExecutor,
                     final OrderUtilHandler orderUtilHandler,
                     final CalculationUtil calculationUtil) {
        this.taskExecutor = taskExecutor;
        this.orderUtilHandler = orderUtilHandler;
        this.calculationUtil = calculationUtil;
    }

    public Observable<OrderEvent> submitOrder(final SubmitParams submitParams) {
        final OrderParams orderParams = submitParams.orderParams();
        final OrderCallReason callReason = orderParams.orderCommand().isConditional()
                ? OrderCallReason.SUBMIT_CONDITIONAL
                : OrderCallReason.SUBMIT;

        return Observable.defer(() -> taskExecutor
            .submitOrder(orderParams)
            .toObservable()
            .flatMap(order -> orderUtilObservable(order, callReason)));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return Observable
            .just(toMergeOrders)
            .filter(orders -> orders.size() >= 2)
            .flatMap(orders -> taskExecutor
                .mergeOrders(mergeOrderLabel, orders)
                .toObservable()
                .flatMap(order -> orderUtilObservable(order, OrderCallReason.MERGE)));
    }

    public Observable<OrderEvent> mergeOrders(final MergeParams mergeParams) {
        final String mergeOrderLabel = mergeParams.mergeOrderLabel();
        final Collection<IOrder> toMergeOrders = mergeParams.toMergeOrders();

        return mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> close(final CloseParams closeParams) {
        logger.info("Called close");
        return Observable
            .just(closeParams.order())
            .filter(order -> !OrderStaticUtil.isClosed.test(order))
            .flatMap(order -> evalCloseParmas(order, closeParams)
                .andThen(orderUtilObservable(order, callReasonForClose(closeParams, order))));
    }

    private OrderCallReason callReasonForClose(final CloseParams closeParams,
                                               final IOrder orderToClose) {
        final double closeAmount = closeParams.partialCloseAmount();
        final double orderAmount = orderToClose.getAmount();
        return closeAmount > 0 && closeAmount < orderAmount
                ? OrderCallReason.PARTIAL_CLOSE
                : OrderCallReason.CLOSE;
    }

    private Completable evalCloseParmas(final IOrder orderToClose,
                                        final CloseParams closeParams) {
        logger.info("Called evalCloseParmas");
        return closeParams.maybePrice().isPresent()
                ? taskExecutor
                    .close(orderToClose,
                           closeParams.partialCloseAmount(),
                           closeParams.maybePrice().get(),
                           closeParams.slippage())
                : taskExecutor
                    .close(orderToClose,
                           closeParams.partialCloseAmount());
    }

    public Observable<OrderEvent> setLabel(final SetLabelParams setLabelParams) {
        final IOrder orderToSetLabel = setLabelParams.order();
        final String newLabel = setLabelParams.newLabel();

        return Observable
            .just(orderToSetLabel)
            .filter(order -> !isLabelSetTo(newLabel).test(order))
            .flatMap(order -> taskExecutor
                .setLabel(order, newLabel)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_LABEL)));
    }

    public Observable<OrderEvent> setGoodTillTime(final SetGTTParams setGTTParams) {
        final IOrder orderToSetGTT = setGTTParams.order();
        final long newGTT = setGTTParams.newGTT();

        return Observable
            .just(orderToSetGTT)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> taskExecutor
                .setGoodTillTime(order, newGTT)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_GTT)));
    }

    public Observable<OrderEvent> setRequestedAmount(final SetAmountParams setAmountParams) {
        final IOrder orderToSetAmount = setAmountParams.order();
        final double newRequestedAmount = setAmountParams.newAmount();

        return Observable
            .just(orderToSetAmount)
            .filter(order -> !isAmountSetTo(newRequestedAmount).test(order))
            .flatMap(order -> taskExecutor
                .setRequestedAmount(order, newRequestedAmount)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_AMOUNT)));
    }

    public Observable<OrderEvent> setOpenPrice(final SetOpenPriceParams setOpenPriceParams) {
        final IOrder orderToSetOpenPrice = setOpenPriceParams.order();
        final double newOpenPrice = setOpenPriceParams.newOpenPrice();

        return Observable
            .just(orderToSetOpenPrice)
            .filter(order -> !isOpenPriceSetTo(newOpenPrice).test(order))
            .flatMap(order -> taskExecutor
                .setOpenPrice(order, newOpenPrice)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_PRICE)));
    }

    public Observable<OrderEvent> setStopLossPrice(final SetSLParams setSLParams) {
        final IOrder orderToSetSL = setSLParams.order();
        final double newSL = setSLParams.setSLTPMode() == SetSLTPMode.PRICE
                ? setSLParams.priceOrPips()
                : calculationUtil.slPriceForPips(orderToSetSL, setSLParams.priceOrPips());

        return Observable
            .just(setSLParams.order())
            .filter(order -> !isSLSetTo(newSL).test(order))
            .flatMap(order -> taskExecutor
                .setStopLossPrice(order,
                                  newSL,
                                  setSLParams.offerSide(),
                                  setSLParams.trailingStep())
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_SL)));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final SetTPParams setTPParams) {
        final IOrder orderToSetTP = setTPParams.order();
        final double newTP = setTPParams.setSLTPMode() == SetSLTPMode.PRICE
                ? setTPParams.priceOrPips()
                : calculationUtil.tpPriceForPips(orderToSetTP, setTPParams.priceOrPips());

        return Observable
            .just(orderToSetTP)
            .filter(order -> !isTPSetTo(newTP).test(order))
            .flatMap(order -> taskExecutor
                .setTakeProfitPrice(order, newTP)
                .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP)));
    }

    private final Observable<OrderEvent> orderUtilObservable(final IOrder order,
                                                             final OrderCallReason orderCallReason) {
        logger.info("Called orderUtilObservable");
        return orderUtilHandler.callObservable(order, orderCallReason);
    }
}
