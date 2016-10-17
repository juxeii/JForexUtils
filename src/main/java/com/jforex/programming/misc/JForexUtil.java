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
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.ClosePositionCommandHandler;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.CancelSLTask;
import com.jforex.programming.order.task.CancelTPTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
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

    private TickQuoteProvider tickQuoteProvider;
    private TickQuoteRepository tickQuoteRepository;
    private BarQuoteProvider barQuoteProvider;
    private BarQuoteRepository barQuoteRepository;

    private PositionFactory positionFactory;
    private PositionUtil positionUtil;
    private OrderEventGateway orderEventGateway;
    private StrategyThreadTask strategyThreadTask;
    private TaskExecutor orderTaskExecutor;
    private OrderUtilHandler orderUtilHandler;
    private BasicTask orderBasicTask;
    private BatchChangeTask orderChangeBatch;
    private MergeTask orderMergeTask;
    private CloseTask orderCloseTask;
    private MergeCommandHandler mergeCommandHandler;
    private ClosePositionCommandHandler closePositionCommandHandler;
    private CancelSLTPAndMergeTask cancelAndMergeSplitter;
    private CancelSLTPTask orderCancelSLAndTP;
    private CancelSLTask orderCancelSL;
    private CancelTPTask orderCancelTP;
    private OrderUtil orderUtil;
    private OrderEventFactory orderEventFactory;
    private final CalculationUtil calculationUtil;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory = new OrderEventTypeDataFactory();

    private final JFHotPublisher<TickQuote> tickQuotePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<BarQuote> barQuotePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<IMessage> messagePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();

    public static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    public static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = checkNotNull(context);

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteProvider);
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();

        historyUtil = new HistoryUtil(history);
    }

    private void initInfrastructure() {
        orderEventFactory = new OrderEventFactory(callRequestPublisher.observable());
        orderEventGateway = new OrderEventGateway(messagePublisher.observable(), orderEventFactory);
    }

    private void initQuoteProvider() {
        tickQuoteRepository = new TickQuoteRepository(tickQuotePublisher.observable(),
                                                      historyUtil,
                                                      context.getSubscribedInstruments());
        tickQuoteProvider = new TickQuoteProvider(tickQuotePublisher.observable(), tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuotePublisher.observable(), historyUtil);
        barQuoteProvider = new BarQuoteProvider(this,
                                                barQuotePublisher.observable(),
                                                barQuoteRepository);
    }

    private void initOrderRelated() {
        strategyThreadTask = new StrategyThreadTask(context);
        positionFactory = new PositionFactory(orderEventGateway.observable());
        positionUtil = new PositionUtil(positionFactory);
        orderUtilHandler = new OrderUtilHandler(orderEventGateway,
                                                orderEventTypeDataFactory,
                                                callRequestPublisher);
        orderTaskExecutor = new TaskExecutor(strategyThreadTask, engine);
        orderBasicTask = new BasicTask(orderTaskExecutor, orderUtilHandler);
        orderChangeBatch = new BatchChangeTask(orderBasicTask);
        orderCancelSL = new CancelSLTask(orderChangeBatch);
        orderCancelTP = new CancelTPTask(orderChangeBatch);
        orderCancelSLAndTP = new CancelSLTPTask(orderCancelSL, orderCancelTP);
        mergeCommandHandler = new MergeCommandHandler(orderCancelSLAndTP, orderBasicTask);
        cancelAndMergeSplitter = new CancelSLTPAndMergeTask(mergeCommandHandler);
        orderMergeTask = new MergeTask(cancelAndMergeSplitter, positionUtil);
        closePositionCommandHandler = new ClosePositionCommandHandler(orderMergeTask,
                                                                      orderChangeBatch,
                                                                      positionUtil);
        orderCloseTask = new CloseTask(closePositionCommandHandler, positionUtil);
        orderUtil = new OrderUtil(orderBasicTask,
                                  orderMergeTask,
                                  orderCloseTask,
                                  positionUtil);
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
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(checkNotNull(instrument),
                                  tickQuoteProvider,
                                  barQuoteProvider,
                                  calculationUtil);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public PositionUtil positionUtil() {
        return positionUtil;
    }

    public void onStop() {
        tickQuotePublisher.unsubscribe();
        barQuotePublisher.unsubscribe();
        messagePublisher.unsubscribe();
        callRequestPublisher.unsubscribe();
    }

    public void onMessage(final IMessage message) {
        messagePublisher.onNext(checkNotNull(message));
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
        return Thread.currentThread().getName();
    }
}
