package com.jforex.programming.test.common;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;

public class OrderParamsForTest extends CommonUtilForTest {

    public static OrderParams paramsBuyEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.BUY)
                .withAmount(0.1)
                .withLabel("TestBuyLabelEURUSD")
                .price(userSettings.defaultOpenPrice())
                .slippage(userSettings.defaultSlippage())
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for buy EURUSD")
                .build();
    }

    public static OrderParams paramsSellEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.SELL)
                .withAmount(0.12)
                .withLabel("TestSellLabelEURUSD")
                .price(userSettings.defaultOpenPrice())
                .slippage(userSettings.defaultSlippage())
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for sell EURUSD")
                .build();
    }
}
