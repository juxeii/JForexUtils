package com.jforex.programming.order;

import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.process.option.CloseOption;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SetAmountOption;
import com.jforex.programming.order.process.option.SetGTTOption;
import com.jforex.programming.order.process.option.SetLabelOption;
import com.jforex.programming.order.process.option.SetOpenPriceOption;
import com.jforex.programming.order.process.option.SetSLOption;
import com.jforex.programming.order.process.option.SetTPOption;
import com.jforex.programming.order.process.option.SubmitOption;
import com.jforex.programming.position.PositionOrders;

import rx.Completable;

public interface OrderUtil {

    public SubmitOption submitBuilder(OrderParams orderParams);

    public MergeOption mergeBuilder(String mergeOrderLabel,
                                    Set<IOrder> toMergeOrders);

    public CloseOption closeBuilder(IOrder orderToClose);

    public SetLabelOption setLabelBuilder(IOrder order,
                                          String newLabel);

    public SetGTTOption setGTTBuilder(IOrder order,
                                      long newGTT);

    public SetAmountOption setAmountBuilder(IOrder order,
                                            double newAmount);

    public SetOpenPriceOption setOpenPriceBuilder(IOrder order,
                                                  double newPrice);

    public SetSLOption setSLBuilder(IOrder order,
                                    double newSL);

    public SetTPOption setTPBuilder(IOrder order,
                                    double newTP);

    public Completable mergePosition(Instrument instrument,
                                     Function<Set<IOrder>, MergeCommand> mergeCommandFactory);

    public Completable mergeAllPositions(Function<Set<IOrder>, MergeCommand> mergeCommandFactory);

    public Completable closePosition(Instrument instrument,
                                     Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                     Function<IOrder, CloseCommand> closeCommandFactory);

    public Completable closeAllPositions(Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                         Function<IOrder, CloseCommand> closeCommandFactory);

    public PositionOrders positionOrders(Instrument instrument);
}
