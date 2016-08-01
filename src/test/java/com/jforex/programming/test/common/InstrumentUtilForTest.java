package com.jforex.programming.test.common;

import com.dukascopy.api.Instrument;

public class InstrumentUtilForTest extends CommonUtilForTest {

    public final Instrument instrumentEURUSD = Instrument.EURUSD;
    public final Instrument instrumentEURJPY = Instrument.EURJPY;
    public final Instrument instrumentEURGBP = Instrument.EURGBP;
    public final Instrument instrumentEURAUD = Instrument.EURAUD;
    public final Instrument instrumentUSDJPY = Instrument.USDJPY;
    public final Instrument instrumentGBPAUD = Instrument.GBPAUD;
    public final Instrument instrumentGBPJPY = Instrument.GBPJPY;
    public final Instrument instrumentGBPUSD = Instrument.GBPUSD;
    public final Instrument instrumentAUDUSD = Instrument.AUDUSD;
    public final Instrument instrumentAUDJPY = Instrument.AUDJPY;

    public final double askEURUSD = 1.10987;
    public final double bidEURUSD = 1.10975;

    public final double askUSDJPY = 124.345;
    public final double bidUSDJPY = 124.341;

    public final double askEURJPY = 134.992;
    public final double bidEURJPY = 134.981;

    public final double askAUDUSD = 1.10348;
    public final double bidAUDUSD = 1.10345;

    public final double askGBPAUD = 1.12321;
    public final double bidGBPAUD = 1.12319;

    public static final int noOfDigitsNonJPYInstrument = 5;
    public static final int noOfDigitsJPYInstrument = 3;
}
