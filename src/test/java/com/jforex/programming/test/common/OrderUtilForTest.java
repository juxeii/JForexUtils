package com.jforex.programming.test.common;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;

public final class OrderUtilForTest extends CommonUtilForTest {

    private static final double defaultOpenPrice = userSettings.defaultOpenPrice();
    private static final double defaultSlippage = userSettings.defaultSlippage();

    public static final void setState(final IOrder orderMock,
                                      final IOrder.State orderState) {
        when(orderMock.getState()).thenReturn(orderState);
    }

    public static final void setAmount(final IOrder orderMock,
                                       final double amount) {
        when(orderMock.getAmount()).thenReturn(amount);
    }

    public static final void setRequestedAmount(final IOrder orderMock,
                                                final double requestedAmount) {
        when(orderMock.getRequestedAmount()).thenReturn(requestedAmount);
    }

    public static final void setGTT(final IOrder orderMock,
                                    final long gtt) {
        when(orderMock.getGoodTillTime()).thenReturn(gtt);
    }

    public static final void setSL(final IOrder orderMock,
                                   final double sl) {
        when(orderMock.getStopLossPrice()).thenReturn(sl);
    }

    public static final void setTP(final IOrder orderMock,
                                   final double tp) {
        when(orderMock.getTakeProfitPrice()).thenReturn(tp);
    }

    public static final void setOpenPrice(final IOrder orderMock,
                                          final double openPrice) {
        when(orderMock.getOpenPrice()).thenReturn(openPrice);
    }

    public static void setLabel(final IOrder orderMock,
                                final String label) {
        when(orderMock.getLabel()).thenReturn(label);
    }

    public static final void setOrderCommand(final IOrder orderMock,
                                             final OrderCommand orderCommand) {
        when(orderMock.getOrderCommand()).thenReturn(orderCommand);
        adaptIsLong(orderMock, orderCommand);
    }

    public static final IOrder spyFromParams(final OrderParams orderParams) {
        final IOrder orderMock = spy(IOrder.class);

        setState(orderMock, IOrder.State.CREATED);
        setLabel(orderMock, orderParams.label());
        when(orderMock.getInstrument()).thenReturn(orderParams.instrument());
        setOrderCommand(orderMock, orderParams.orderCommand());
        setAmount(orderMock, orderParams.amount());
        setSL(orderMock, orderParams.stopLossPrice());
        setTP(orderMock, orderParams.takeProfitPrice());
        setGTT(orderMock, orderParams.goodTillTime());
        when(orderMock.getComment()).thenReturn(orderParams.comment());
        adaptIsLong(orderMock, orderParams.orderCommand());

        return orderMock;
    }

    private static final void adaptIsLong(final IOrder orderMock,
                                          final OrderCommand orderCommand) {
        if (OrderStaticUtil.buyOrderCommands.contains(orderCommand))
            when(orderMock.isLong()).thenReturn(true);
        else
            when(orderMock.isLong()).thenReturn(false);
    }

    public static final IOrder buyOrderEURUSD() {
        return spyFromParams(paramsBuyEURUSD());
    }

    public static final IOrder sellOrderEURUSD() {
        return spyFromParams(paramsSellEURUSD());
    }

    public static final IOrder orderAUDUSD() {
        return spyFromParams(paramsSellAUDUSD());
    }

    public static final OrderParams paramsBuyEURUSD() {
        return OrderParams
                .forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.BUY)
                .withAmount(0.1)
                .withLabel("TestBuyLabelEURUSD")
                .price(defaultOpenPrice)
                .slippage(defaultSlippage)
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for buy EURUSD")
                .build();
    }

    public static final OrderParams paramsSellEURUSD() {
        return OrderParams
                .forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.SELL)
                .withAmount(0.12)
                .withLabel("TestSellLabelEURUSD")
                .price(defaultOpenPrice)
                .slippage(defaultSlippage)
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for sell EURUSD")
                .build();
    }

    public static OrderParams paramsSellAUDUSD() {
        return OrderParams
                .forInstrument(Instrument.AUDUSD)
                .withOrderCommand(OrderCommand.SELL)
                .withAmount(0.12)
                .withLabel("TestSellLabelAUDUSD")
                .build();
    }
}
