package com.jforex.programming.test.common;

import static com.jforex.programming.misc.JForexUtil.uss;

import com.jforex.programming.order.OrderParams;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;

public class OrderParamsForTest {

    public static OrderParams paramsBuyEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                          .withOrderCommand(OrderCommand.BUY)
                          .withAmount(0.1)
                          .withLabel("TestBuyLabelEURUSD")
                          .price(uss.ORDER_DEFAULT_PRICE())
                          .slippage(uss.ORDER_DEFAULT_SLIPPAGE())
                          .stopLossPrice(1.32456)
                          .takeProfitPrice(1.32556)
                          .goodTillTime(uss.ORDER_DEFAULT_GOOD_TILL_TIME())
                          .comment("Test comment for buy EURUSD")
                          .build();
    }

    public static OrderParams paramsSellEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                          .withOrderCommand(OrderCommand.SELL)
                          .withAmount(0.12)
                          .withLabel("TestSellLabelEURUSD")
                          .price(uss.ORDER_DEFAULT_PRICE())
                          .slippage(uss.ORDER_DEFAULT_SLIPPAGE())
                          .stopLossPrice(1.32456)
                          .takeProfitPrice(1.32556)
                          .goodTillTime(uss.ORDER_DEFAULT_GOOD_TILL_TIME())
                          .comment("Test comment for sell EURUSD")
                          .build();
    }

    public static OrderParams paramsUSDJPY() {
        return OrderParams.forInstrument(Instrument.USDJPY)
                          .withOrderCommand(OrderCommand.SELL)
                          .withAmount(0.1)
                          .withLabel("TestLabelUSDJPY")
                          .price(uss.ORDER_DEFAULT_PRICE())
                          .slippage(uss.ORDER_DEFAULT_SLIPPAGE())
                          .stopLossPrice(132.456)
                          .takeProfitPrice(132.556)
                          .goodTillTime(uss.ORDER_DEFAULT_GOOD_TILL_TIME())
                          .comment("Test comment for USDJPY")
                          .build();
    }
}
