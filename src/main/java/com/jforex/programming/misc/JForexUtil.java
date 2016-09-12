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
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilBuilder;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.position.PositionClose;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMerge;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

public class JForexUtil {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;
    private HistoryUtil historyUtil;
    private IDataService dataService;
    private IEngineUtil engineUtil;

    private TickQuoteHandler tickQuoteHandler;
    private TickQuoteRepository tickQuoteRepository;
    private BarQuoteHandler barQuoteHandler;
    private BarQuoteRepository barQuoteRepository;

    private PositionFactory positionFactory;
    private OrderEventGateway orderEventGateway;
    private TaskExecutor orderCallExecutor;
    private OrderUtilHandler orderUtilHandler;
    private OrderUtil orderUtil;
    private OrderUtilBuilder orderUtilBuilder;
    private PositionUtil positionUtil;
    private PositionMerge positionMerge;
    private PositionClose positionClose;
    private OrderUtilCompletable orderUtilCompletable;
    private CommandUtil commandUtil;
    private final OrderEventFactory messageToOrderEvent = new OrderEventFactory();

    private final CalculationUtil calculationUtil;

    private final JFHotSubject<TickQuote> tickQuoteSubject = new JFHotSubject<>();
    private final JFHotSubject<BarQuote> barQuoteSubject = new JFHotSubject<>();
    private final JFHotSubject<IMessage> messageSubject = new JFHotSubject<>();

    public static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    public static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = checkNotNull(context);

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteHandler);
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();

        historyUtil = new HistoryUtil(history);
    }

    private void initInfrastructure() {
        orderEventGateway = new OrderEventGateway(messageSubject.observable(), messageToOrderEvent);
    }

    private void initQuoteProvider() {
        tickQuoteRepository = new TickQuoteRepository(tickQuoteSubject.observable(),
                                                      historyUtil,
                                                      context.getSubscribedInstruments());
        tickQuoteHandler = new TickQuoteHandler(tickQuoteSubject.observable(),
                                                tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuoteSubject.observable(),
                                                    historyUtil);
        barQuoteHandler = new BarQuoteHandler(this,
                                              barQuoteSubject.observable(),
                                              barQuoteRepository);
    }

    private void initOrderRelated() {
        orderCallExecutor = new TaskExecutor(context);
        positionFactory = new PositionFactory(orderEventGateway.observable());
        orderUtilHandler = new OrderUtilHandler(orderCallExecutor, orderEventGateway);
        engineUtil = new IEngineUtil(engine);
        orderUtilCompletable = new OrderUtilCompletable(orderUtilHandler, positionFactory);
        commandUtil = new CommandUtil(orderUtilCompletable);
        orderUtilBuilder = new OrderUtilBuilder(engineUtil, orderUtilCompletable);
        positionMerge = new PositionMerge(orderUtilCompletable, positionFactory);
        positionClose = new PositionClose(positionMerge,
                                          positionFactory,
                                          commandUtil);
        positionUtil = new PositionUtil(positionMerge,
                                        positionClose,
                                        positionFactory);
        orderUtil = new OrderUtil(orderUtilBuilder,
                                  positionUtil,
                                  orderUtilCompletable);
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
        return tickQuoteHandler;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteHandler;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(checkNotNull(instrument),
                                  tickQuoteHandler,
                                  barQuoteHandler);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public CommandUtil commandUtil() {
        return commandUtil;
    }

    public void onStop() {
        tickQuoteSubject.unsubscribe();
        barQuoteSubject.unsubscribe();
        messageSubject.unsubscribe();
    }

    public void onMessage(final IMessage message) {
        messageSubject.onNext(checkNotNull(message));
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        checkNotNull(instrument);
        checkNotNull(tick);

        if (shouldForwardQuote(tick.getTime())) {
            final TickQuote tickQuote = new TickQuote(instrument, tick);
            tickQuoteSubject.onNext(tickQuote);
        }
    }

    private boolean shouldForwardQuote(final long time) {
        return !userSettings.enableWeekendQuoteFilter() || !isMarketClosed(time);
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
                                       final IBar askBar) {
        if (shouldForwardQuote(askBar.getTime())) {
            final BarParams quoteParams = BarParams
                .forInstrument(instrument)
                .period(period)
                .offerSide(offerside);
            final BarQuote askBarQuote = new BarQuote(askBar, quoteParams);
            barQuoteSubject.onNext(askBarQuote);
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
        return Thread.currentThread().getName();
    }
}
