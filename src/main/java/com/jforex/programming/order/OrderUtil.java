package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.spec.BasicSpec;
import com.jforex.programming.order.spec.ComplexMergeSpec;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderUtil {

    private final BasicTask basicTask;
    private final MergeTask mergeTask;
    private final CloseTask closeTask;
    private final PositionUtil positionUtil;

    public OrderUtil(final BasicTask basicTask,
                     final MergeTask mergeTask,
                     final CloseTask closeTask,
                     final PositionUtil positionUtil) {
        this.basicTask = basicTask;
        this.mergeTask = mergeTask;
        this.closeTask = closeTask;
        this.positionUtil = positionUtil;
    }

    public BasicSpec.SubmitBuilder submitOrder(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return BasicSpec.forSubmit(basicTask.submitOrder(orderParams));
    }

    public BasicSpec.MergeBuilder mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        checkNotNull(mergeOrderLabel);
        checkNotNull(toMergeOrders);

        return BasicSpec.forMerge(basicTask.mergeOrders(mergeOrderLabel, toMergeOrders));
    }

    public BasicSpec.CloseBuilder close(final IOrder order) {
        checkNotNull(order);

        return BasicSpec.forClose(basicTask.close(order));
    }

    public BasicSpec.CloseBuilder close(final CloseParams closeParams) {
        checkNotNull(closeParams);

        return BasicSpec.forClose(basicTask.close(closeParams));
    }

    public BasicSpec.SetLabelBuilder setLabel(final IOrder order,
                                              final String label) {
        checkNotNull(order);

        return BasicSpec.forSetLabel(basicTask.setLabel(order, label));
    }

    public BasicSpec.SetGTTBuilder setGoodTillTime(final IOrder order,
                                                   final long newGTT) {
        checkNotNull(order);

        return BasicSpec.forSetGTT(basicTask.setGoodTillTime(order, newGTT));
    }

    public BasicSpec.SetAmountBuilder setRequestedAmount(final IOrder order,
                                                         final double newRequestedAmount) {
        checkNotNull(order);

        return BasicSpec.forSetAmount(basicTask.setRequestedAmount(order, newRequestedAmount));
    }

    public BasicSpec.SetOpenPriceBuilder setOpenPrice(final IOrder order,
                                                      final double newOpenPrice) {
        checkNotNull(order);

        return BasicSpec.forSetOpenPrice(basicTask.setOpenPrice(order, newOpenPrice));
    }

    public BasicSpec.SetSLBuilder setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        checkNotNull(order);

        return BasicSpec.forSetSL(basicTask.setStopLossPrice(order, newSL));
    }

    public BasicSpec.SetSLBuilder setStopLossPrice(final SetSLParams setSLParams) {
        checkNotNull(setSLParams);

        return BasicSpec.forSetSL(basicTask.setStopLossPrice(setSLParams));
    }

    public BasicSpec.SetSLBuilder setStopLossForPips(final IOrder order,
                                                     final double pips) {
        checkNotNull(order);

        return BasicSpec.forSetSL(basicTask.setStopLossForPips(order, pips));
    }

    public BasicSpec.SetTPBuilder setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        checkNotNull(order);

        return BasicSpec.forSetTP(basicTask.setTakeProfitPrice(order, newTP));
    }

    public BasicSpec.SetTPBuilder setTakeProfitForPips(final IOrder order,
                                                       final double pips) {
        checkNotNull(order);

        return BasicSpec.forSetTP(basicTask.setTakeProfitForPips(order, pips));
    }

    public Observable<OrderEvent> mergeOrders(final Collection<IOrder> toMergeOrders,
                                              final MergeParams mergeParams) {
        checkNotNull(toMergeOrders);
        checkNotNull(mergeParams);

        return mergeTask.merge(toMergeOrders, mergeParams);
    }

    public ComplexMergeSpec.ComplexMergeBuilder mergeOrdersTest(final String mergeOrderLabel,
                                                                final Collection<IOrder> toMergeOrders) {
        checkNotNull(toMergeOrders);
        checkNotNull(toMergeOrders);

        return ComplexMergeSpec.forMerge(mergeOrderLabel,
                                         toMergeOrders,
                                         mergeTask);
    }

    public Observable<OrderEvent> mergePosition(final Instrument instrument,
                                                final MergeParams mergeParams) {
        checkNotNull(instrument);
        checkNotNull(mergeParams);

        return mergeTask.mergePosition(instrument, mergeParams);
    }

    public Observable<OrderEvent> mergeAllPositions(final Function<Instrument, MergeParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return mergeTask.mergeAllPositions(paramsFactory);
    }

    public Observable<OrderEvent> closePosition(final ClosePositionParams positionParams) {
        checkNotNull(positionParams);

        return closeTask.close(positionParams);
    }

    public Observable<OrderEvent> closeAllPositions(final Function<Instrument, ClosePositionParams> paramsFactory) {
        checkNotNull(paramsFactory);

        return closeTask.closeAllPositions(paramsFactory);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        checkNotNull(instrument);

        return positionUtil.positionOrders(instrument);
    }
}
