package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderUtilCommand;
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

public class OrderUtil {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;
    private final IEngineUtil engineUtil;

    public OrderUtil(final OrderUtilHandler orderUtilHandler,
                     final PositionFactory positionFactory,
                     final IEngineUtil engineUtil) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
        this.engineUtil = engineUtil;
    }

    public final <T extends OrderUtilCommand> void startBatchCommand(final Set<IOrder> orders,
                                                                     final Function<IOrder, T> batchCommand) {
        orders.forEach(order -> batchCommand.apply(order).start());
    }

    public final void closePosition(final Instrument instrument,
                                    final Function<IOrder, CloseCommand> closeCommand) {
        final Position position = position(instrument);
        final Set<IOrder> ordersToClose = position.filledOrOpened();

        startBatchCommand(ordersToClose, closeCommand);
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    public Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public void addOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }

    public final SubmitCommand.Option submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    orderUtilHandler,
                                    engineUtil,
                                    this);
    }

    public final MergeCommand.Option mergeBuilder(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   orderUtilHandler,
                                   engineUtil,
                                   this);
    }

    public final CloseCommand.Option closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose,
                                   orderUtilHandler,
                                   this);
    }

    public final SetLabelCommand.Option setLabelBuilder(final IOrder order,
                                                        final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      orderUtilHandler);
    }

    public final SetGTTCommand.Option setGTTBuilder(final IOrder order,
                                                    final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    orderUtilHandler);
    }

    public final SetAmountCommand.Option setAmountBuilder(final IOrder order,
                                                          final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       orderUtilHandler);
    }

    public final SetOpenPriceCommand.Option setOpenPriceBuilder(final IOrder order,
                                                                final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          orderUtilHandler);
    }

    public final SetSLCommand.Option setSLBuilder(final IOrder order,
                                                  final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   orderUtilHandler);
    }

    public final SetTPCommand.Option setTPBuilder(final IOrder order,
                                                  final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   orderUtilHandler);
    }
}
