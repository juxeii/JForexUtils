package com.jforex.programming.article;

import java.util.Currency;

import com.jforex.programming.currency.CurrencyBuilder;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderParams;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;

/* Remove both annotations if you develop a standalone app */
@RequiresFullAccess
/* Change the path to the jar to your path! */
@Library("D:/programs/JForex/libs/JForexUtils-0.9.35.jar")
public class RunningExamplePart2 implements IStrategy {

    private JForexUtil jForexUtil;
    private final Instrument instrumentEURUSD = Instrument.EURUSD;
    private final ICurrency eurCurrency = CurrencyBuilder.instanceFromName("EUR");
    private final ICurrency usdCurrency = CurrencyBuilder.instanceFromName("USD");

    @Override
    public void onStart(final IContext context) throws JFException {
        jForexUtil = new JForexUtil(context);

        /********************************/
        /*** CalculationUtil examples ***/
        /********************************/

        // Some static methods of CalculationUtil
        final double scaledAmount = CalculationUtil.scaleToPlatformAmount(123456.78);
        final double pipDistance = CalculationUtil.pipDistance(instrumentEURUSD, 1.12887, 1.12743);
        final double addedPipsToPrice = CalculationUtil.addPips(instrumentEURUSD, 1.12887, 10.4);
        final double scaledPips = CalculationUtil.scalePipsToInstrument(12.4, instrumentEURUSD);

        System.out.println("Scaled amount of  123456.78 units is " + scaledAmount + " for JForex platform.");
        System.out.println("pipDistance for EUR/USD from   1.12887 to 1.12743 is " + pipDistance);
        System.out.println("Adding 10.4 pips to 1.12887 for EUR/USD is " + addedPipsToPrice);
        System.out.println("Scaled pips of 12.4 to price format of EUR/USD is " + scaledPips);

        // Conversion calculations needs instance of CalculationUtil
        final CalculationUtil calculationUtil = jForexUtil.calculationUtil();
        final double convertedAmount =
                calculationUtil.convertAmount(12540,
                                              eurCurrency,
                                              usdCurrency,
                                              OfferSide.ASK);
        final double convertedAmountForInstrument =
                calculationUtil.convertAmountForInstrument(12540,
                                                           Instrument.EURUSD,
                                                           Instrument.GBPAUD,
                                                           OfferSide.ASK);
        final double pipValueInCurrency =
                calculationUtil.pipValueInCurrency(instrumentEURUSD,
                                                   12540,
                                                   usdCurrency,
                                                   OfferSide.ASK);

        System.out.println("Converted amount of 12540 in EUR is " + convertedAmount + " in USD for ASK side");
        System.out.println("Converted amount for EUR/USD of 12540 EUR is "
                + convertedAmountForInstrument + " amount for GBP/AUD for ASK side");
        System.out.println("Pip value for EUR/USD of 12540 EUR is " + pipValueInCurrency + " for ASK side");

        /********************************/
        /*** InstrumentUtil examples ***/
        /********************************/

        // Create an InstrumentUtil instance and get the latest quotes
        final InstrumentUtil utilEURUSD = jForexUtil.instrumentUtil(Instrument.EURUSD);

        final ITick latestTick = utilEURUSD.tick();
        final IBar latestAskBar = utilEURUSD.askBar(Period.ONE_MIN);
        final IBar latestBidBar = utilEURUSD.bidBar(Period.TEN_MINS);
        final double ask = utilEURUSD.ask();
        final double bid = utilEURUSD.bid();
        final double spread = utilEURUSD.spread();

        // Other useful attributes of an InstrumentUtil instance
        final int noOfDigits = utilEURUSD.numberOfDigits();
        final String instrumentStringNoSlash = utilEURUSD.toStringNoSeparator();
        final Currency baseCurrency = utilEURUSD.baseJavaCurrency();
        final Currency quoteCurrency = utilEURUSD.quoteJavaCurrency();

        InstrumentUtil.numberOfDigits(instrumentEURUSD);
        InstrumentUtil.toStringNoSeparator(instrumentEURUSD);

        System.out.println("Attributes of EURUSD from InstrumentUtil:");
        System.out.println("Latest time of tick: " + latestTick.getTime());
        System.out.println("Latest high of askBar: " + latestAskBar.getHigh());
        System.out.println("Latest low of bidBar: " + latestBidBar.getLow());
        System.out.println("Latest ask: " + ask);
        System.out.println("Latest bid: " + bid);
        System.out.println("Current spread: " + spread);
        System.out.println("Number of digits: " + noOfDigits);
        System.out.println("Instrument as string no slash: " + instrumentStringNoSlash);
        System.out.println("Base currency code: " + baseCurrency.getCurrencyCode());
        System.out.println("Quote currency code: " + quoteCurrency.getCurrencyCode());

        OrderParams.forInstrument(Instrument.EURUSD)
                   .withOrderCommand(OrderCommand.BUY)
                   .withAmount(0.002)
                   .withLabel("TestLabel1")
                   .build();

        final OrderParams orderParamsEURUSDFull =
                OrderParams.forInstrument(Instrument.EURUSD)
                           .withOrderCommand(OrderCommand.BUY)
                           .withAmount(0.002)
                           .withLabel("TestLabel2")
                           .price(0)
                           .goodTillTime(0L)
                           .slippage(2.0)
                           .stopLossPrice(0)
                           .takeProfitPrice(0)
                           .comment("Test Comment")
                           .build();

        orderParamsEURUSDFull.clone()
                             .withAmount(0.003)
                             .build();

        System.out.println("Full specified order parameters are:");
        System.out.println("Instrument: " + orderParamsEURUSDFull.instrument());
        System.out.println("Command: " + orderParamsEURUSDFull.orderCommand());
        System.out.println("Amount: " + orderParamsEURUSDFull.amount());
        System.out.println("Label: " + orderParamsEURUSDFull.label());
        System.out.println("Price: " + orderParamsEURUSDFull.price());
        System.out.println("GTT: " + orderParamsEURUSDFull.goodTillTime());
        System.out.println("Slippage: " + orderParamsEURUSDFull.slippage());
        System.out.println("SL price: " + orderParamsEURUSDFull.stopLossPrice());
        System.out.println("TP price: " + orderParamsEURUSDFull.takeProfitPrice());
        System.out.println("Comment: " + orderParamsEURUSDFull.comment());
    }

    @Override
    public void onStop() throws JFException {
        jForexUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) throws JFException {
        jForexUtil.onMessage(message);
    }

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        jForexUtil.onTick(instrument, tick);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        jForexUtil.onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public void onAccount(final IAccount account) throws JFException {

    }
}
