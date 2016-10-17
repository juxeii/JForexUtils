package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.object.OrderObjects;
import com.jforex.programming.object.QuoteObjects;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

public class JForexUtil {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;
    private HistoryUtil historyUtil;
    private IDataService dataService;

    private QuoteObjects quoteObjects;
    private OrderObjects orderObject;
    private final CalculationUtil calculationUtil;

    private final JFHotPublisher<TickQuote> tickQuotePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<BarQuote> barQuotePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<IMessage> messagePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();

    public static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    public static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = checkNotNull(context);

        initContextRelated();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(quoteObjects.tickQuoteProvider());
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();

        historyUtil = new HistoryUtil(history);
    }

    private void initQuoteProvider() {
        quoteObjects = new QuoteObjects(this,
                                        historyUtil,
                                        tickQuotePublisher,
                                        barQuotePublisher);
    }

    private void initOrderRelated() {
        orderObject = new OrderObjects(context,
                                       messagePublisher,
                                       callRequestPublisher);
    }

    public IContext context() {
        return context;
    }

    public IEngine engine() {
        return engine;
    }

    public IAccount account() {
        return account;
    }

    public IHistory history() {
        return history;
    }

    public HistoryUtil historyUtil() {
        return historyUtil;
    }

    public TickQuoteProvider tickQuoteProvider() {
        return quoteObjects.tickQuoteProvider();
    }

    public BarQuoteProvider barQuoteProvider() {
        return quoteObjects.barQuoteProvider();
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        checkNotNull(instrument);

        return new InstrumentUtil(instrument,
                                  quoteObjects.tickQuoteProvider(),
                                  quoteObjects.barQuoteProvider(),
                                  calculationUtil);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderObject.orderUtil();
    }

    public PositionUtil positionUtil() {
        return orderObject.positionUtil();
    }

    public void onStop() {
        tickQuotePublisher.unsubscribe();
        barQuotePublisher.unsubscribe();
        messagePublisher.unsubscribe();
        callRequestPublisher.unsubscribe();
    }

    public void onMessage(final IMessage message) {
        checkNotNull(message);

        messagePublisher.onNext(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        checkNotNull(instrument);
        checkNotNull(tick);

        if (shouldForwardQuote(tick.getTime())) {
            final TickQuote tickQuote = new TickQuote(instrument, tick);
            tickQuotePublisher.onNext(tickQuote);
        }
    }

    private boolean shouldForwardQuote(final long time) {
        return !userSettings.enableWeekendQuoteFilter()
                || !isMarketClosed(time);
    }

    public boolean isMarketClosed() {
        return isMarketClosed(DateTimeUtil.localMillisNow());
    }

    public boolean isMarketClosed(final long time) {
        return dataService.isOfflineTime(time);
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        checkNotNull(instrument);
        checkNotNull(period);
        checkNotNull(askBar);
        checkNotNull(bidBar);

        onOfferSidedBar(instrument, period, OfferSide.ASK, askBar);
        onOfferSidedBar(instrument, period, OfferSide.BID, bidBar);
    }

    private final void onOfferSidedBar(final Instrument instrument,
                                       final Period period,
                                       final OfferSide offerside,
                                       final IBar bar) {
        if (shouldForwardQuote(bar.getTime())) {
            final BarParams quoteParams = BarParams
                .forInstrument(instrument)
                .period(period)
                .offerSide(offerside);
            final BarQuote barQuote = new BarQuote(bar, quoteParams);
            barQuotePublisher.onNext(barQuote);
        }
    }

    public void subscribeToBarsFeed(final BarParams barParams) {
        checkNotNull(barParams);

        context.subscribeToBarsFeed(barParams.instrument(),
                                    barParams.period(),
                                    barParams.offerSide(),
                                    this::onOfferSidedBar);
    }

    public static final boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static final String threadName() {
        return Thread
            .currentThread()
            .getName();
    }
}
