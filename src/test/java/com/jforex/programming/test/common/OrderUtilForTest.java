package com.jforex.programming.test.common;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.settings.UserSettings;

public class OrderUtilForTest {

    private static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private static final double defaultOpenPrice = userSettings.defaultOpenPrice();
    private static final double defaultSlippage = userSettings.defaultSlippage();

    public final void setState(final IOrder orderMock,
                               final IOrder.State orderState) {
        when(orderMock.getState()).thenReturn(orderState);
    }

    public final void setAmount(final IOrder orderMock,
                                final double amount) {
        when(orderMock.getAmount()).thenReturn(amount);
    }

    public final void setRequestedAmount(final IOrder orderMock,
                                         final double requestedAmount) {
        when(orderMock.getRequestedAmount()).thenReturn(requestedAmount);
    }

    public final void setGTT(final IOrder orderMock,
                             final long gtt) {
        when(orderMock.getGoodTillTime()).thenReturn(gtt);
    }

    public final void setSL(final IOrder orderMock,
                            final double sl) {
        when(orderMock.getStopLossPrice()).thenReturn(sl);
    }

    public final void setTP(final IOrder orderMock,
                            final double tp) {
        when(orderMock.getTakeProfitPrice()).thenReturn(tp);
    }

    public final void setOpenPrice(final IOrder orderMock,
                                   final double openPrice) {
        when(orderMock.getOpenPrice()).thenReturn(openPrice);
    }

    public void setLabel(final IOrder orderMock,
                         final String label) {
        when(orderMock.getLabel()).thenReturn(label);
    }

    public final void setOrderCommand(final IOrder orderMock,
                                      final OrderCommand orderCommand) {
        when(orderMock.getOrderCommand()).thenReturn(orderCommand);
        adaptIsLong(orderMock, orderCommand);
    }

    public final void setPLInAccountCurrency(final IOrder orderMock,
                                             final double pl) {
        when(orderMock.getProfitLossInAccountCurrency()).thenReturn(pl);
    }

    public final void setPLInPips(final IOrder orderMock,
                                  final double pips) {
        when(orderMock.getProfitLossInPips()).thenReturn(pips);
    }

    public final IOrder spyFromParams(final OrderParams orderParams) {
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

    public final IOrder buyOrderEURUSD() {
        return spyFromParams(buyParamsEURUSD());
    }

    public final IOrder buyOrderEURUSD2() {
        return spyFromParams(buyParamsEURUSD2());
    }

    public final IOrder sellOrderEURUSD() {
        return spyFromParams(sellParamsEURUSD());
    }

    public final IOrder buyOrderAUDUSD() {
        return spyFromParams(buyParamsAUDUSD());
    }

    public final IOrder sellOrderAUDUSD() {
        return spyFromParams(sellParamsAUDUSD());
    }

    public final OrderParams buyParamsEURUSD() {
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

    public final OrderParams buyParamsEURUSD2() {
        return OrderParams
            .forInstrument(Instrument.EURUSD)
            .withOrderCommand(OrderCommand.BUY)
            .withAmount(0.12)
            .withLabel("TestBuyLabelEURUSD2")
            .price(defaultOpenPrice)
            .slippage(defaultSlippage)
            .stopLossPrice(1.32456)
            .takeProfitPrice(1.32556)
            .goodTillTime(userSettings.defaultGTT())
            .comment("Test comment for buy EURUSD2")
            .build();
    }

    public final OrderParams sellParamsEURUSD() {
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

    public OrderParams buyParamsAUDUSD() {
        return OrderParams
            .forInstrument(Instrument.AUDUSD)
            .withOrderCommand(OrderCommand.BUY)
            .withAmount(0.1)
            .withLabel("TestBuyLabelAUDUSD")
            .build();
    }

    public OrderParams sellParamsAUDUSD() {
        return OrderParams
            .forInstrument(Instrument.AUDUSD)
            .withOrderCommand(OrderCommand.SELL)
            .withAmount(0.12)
            .withLabel("TestSellLabelAUDUSD")
            .build();
    }
}
