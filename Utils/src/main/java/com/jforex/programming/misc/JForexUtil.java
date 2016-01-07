package com.jforex.programming.misc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.mm.RiskPercentMM;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeEvaluator;
import com.jforex.programming.order.event.OrderEventTypeEvaluatorByMaps;
import com.jforex.programming.order.event.OrderMessageData;
import com.jforex.programming.position.NoRestorePolicy;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionRepository;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

import rx.Observable;
import rx.Subscription;

public class JForexUtil implements MessageConsumer {

    private final IContext context;
    private IAccount account;
    private IHistory history;

    private ConcurrentUtil concurrentUtil;
    private EngineCallWrapper engineCallWrapper;

    private TickQuoteProvider tickQuoteProvider;
    private BarQuoteProvider barQuoteProvider;

    private OrderUtil orderUtil;
    private PositionRepository positionRepository;
    private OrderEventGateway orderEventGateway;
    private OrderCallExecutor orderCallRunner;

    private final CalculationUtil calculationUtil;
    private final RiskPercentMM riskPercentMM;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final RestoreSLTPPolicy defaultRestorePolicy = new NoRestorePolicy();

    private final JFEventPublisherForRx<IMessage> messagePublisherForRx = new JFEventPublisherForRx<IMessage>();
    private Observable<IMessage> messageObservable;

    private final JFEventPublisherForRx<TickQuote> tickQuotePublisherForRx = new JFEventPublisherForRx<TickQuote>();
    private Observable<TickQuote> tickObservable;

    private final JFEventPublisherForRx<BarQuote> barQuotePublisherForRx = new JFEventPublisherForRx<BarQuote>();
    private Observable<BarQuote> barObservable;

    private final OrderEventTypeEvaluator messageDataToEventByMaps = new OrderEventTypeEvaluatorByMaps();
    private Subscription connectionMonitorSubscription;
    private Subscription eventGatewaySubscription;

    public final static PlatformSettings pfs = ConfigFactory.create(PlatformSettings.class);
    public final static UserSettings uss = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = context;

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteProvider);
        riskPercentMM = new RiskPercentMM(account, calculationUtil);
    }

    private void initContextRelated() {
        account = context.getAccount();
        history = context.getHistory();
        engineCallWrapper = new EngineCallWrapper(context.getEngine());
        concurrentUtil = new ConcurrentUtil(context, executorService);
    }

    private void initInfrastructure() {
        orderEventGateway = new OrderEventGateway(messageDataToEventByMaps);

        messageObservable = Observable.create(messagePublisherForRx::subscribe);
        eventGatewaySubscription = messageObservable.filter(message -> message.getOrder() != null)
                                                    .map(OrderMessageData::new)
                                                    .subscribe(orderEventGateway::onOrderMessageData);
    }

    private void initQuoteProvider() {
        tickObservable = Observable.create(tickQuotePublisherForRx::subscribe);
        tickQuoteProvider = new TickQuoteProvider(tickObservable, history);

        barObservable = Observable.create(barQuotePublisherForRx::subscribe);
        barQuoteProvider = new BarQuoteProvider(barObservable, history);

    }

    private void initOrderRelated() {
        orderCallRunner = new OrderCallExecutor(concurrentUtil);
        orderUtil = new OrderUtil(orderCallRunner,
                                  engineCallWrapper,
                                  orderEventGateway);
        positionRepository = new PositionRepository(orderUtil, orderEventGateway.observable());
    }

    public IContext context() {
        return context;
    }

    public Position position(final Instrument instrument) {
        return position(instrument, defaultRestorePolicy);
    }

    public Position position(final Instrument instrument,
                             final RestoreSLTPPolicy restoreSLTPPolicy) {
        return positionRepository.forInstrument(instrument, restoreSLTPPolicy);
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(instrument, tickQuoteProvider, barQuoteProvider);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public ConcurrentUtil concurrentUtil() {
        return concurrentUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public void closeAllPositions() {
        positionRepository.all().forEach(Position::close);
    }

    public RiskPercentMM riskPercentMM() {
        return riskPercentMM;
    }

    public void onStop() {
        connectionMonitorSubscription.unsubscribe();
        eventGatewaySubscription.unsubscribe();
        concurrentUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) {
        messagePublisherForRx.onJFEvent(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        tickQuotePublisherForRx.onJFEvent(new TickQuote(instrument, tick));
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        barQuotePublisherForRx.onJFEvent(new BarQuote(instrument, period, askBar, bidBar));
    }
}
