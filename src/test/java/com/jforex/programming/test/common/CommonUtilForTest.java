package com.jforex.programming.test.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ITesterClient;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderProcessState;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import nl.jqno.equalsverifier.EqualsVerifier;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected JForexUtil jforexUtilMock;
    @Mock
    protected IClient clientMock;
    @Mock
    protected ITesterClient testerClientMock;
    @Mock
    protected IContext contextMock;
    @Mock
    protected IEngine engineMock;
    @Mock
    protected IHistory historyMock;
    @Mock
    protected IAccount accountMock;
    @Mock
    protected IDataService dataServiceMock;
    @Mock
    protected HistoryUtil historyUtilMock;
    @Mock
    protected TickQuoteHandler tickQuoteHandlerMock;
    @Mock
    protected BarQuoteHandler barQuoteHandlerMock;

    protected JFException jfException = new JFException("JFException for test");

    protected static final String jnlpAddress = "http://jnlp.test.address";
    protected static final String username = "username";
    protected static final String password = "password";
    protected static final String pin = "1234";
    protected static final LoginCredentials loginCredentials = new LoginCredentials(jnlpAddress,
                                                                                    username,
                                                                                    password);
    protected static final LoginCredentials loginCredentialsWithPin = new LoginCredentials(jnlpAddress,
                                                                                           username,
                                                                                           password,
                                                                                           pin);

    protected static final OrderUtilForTest orderUtilForTest = new OrderUtilForTest();
    protected final OrderParams buyParamsEURUSD = orderUtilForTest.buyParamsEURUSD();
    protected final OrderParams sellParamsEURUSD = orderUtilForTest.sellParamsEURUSD();
    protected final IOrder buyOrderEURUSD = orderUtilForTest.buyOrderEURUSD();
    protected final IOrder buyOrderEURUSD2 = orderUtilForTest.buyOrderEURUSD2();
    protected final IOrder sellOrderEURUSD = orderUtilForTest.sellOrderEURUSD();
    protected final IOrder buyOrderAUDUSD = orderUtilForTest.buyOrderAUDUSD();
    protected final IOrder sellOrderAUDUSD = orderUtilForTest.sellOrderAUDUSD();
    protected static final RxTestUtil rxTestUtil = RxTestUtil.get();
    protected static final PlatformSettings platformSettings = JForexUtil.platformSettings;
    protected static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    protected static final double noSL = platformSettings.noSLPrice();
    protected static final double noTP = platformSettings.noTPPrice();

    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);

        when(contextMock.getEngine()).thenReturn(engineMock);
        when(contextMock.getAccount()).thenReturn(accountMock);
        when(contextMock.getHistory()).thenReturn(historyMock);
        when(contextMock.getDataService()).thenReturn(dataServiceMock);

        when(jforexUtilMock.context()).thenReturn(contextMock);
        when(jforexUtilMock.engine()).thenReturn(engineMock);
        when(jforexUtilMock.account()).thenReturn(accountMock);
        when(jforexUtilMock.history()).thenReturn(historyMock);
        when(jforexUtilMock.historyUtil()).thenReturn(historyUtilMock);

        coverageOnEnumsCorrection();
    }

    public static final void setStrategyThread() {
        setThreadName(platformSettings.strategyThreadPrefix());
    }

    public static final void setNotStrategyThread() {
        setThreadName("Not" + platformSettings.strategyThreadPrefix());
    }

    public static final void setThreadName(final String threadName) {
        Thread.currentThread().setName(threadName);
    }

    protected void assertPrivateConstructor(final Class<?> clazz) throws Exception {
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    protected final IMessage mockForIMessage(final IOrder order,
                                             final IMessage.Type type,
                                             final Set<Reason> reasons) {
        final IMessage messageMock = spy(IMessage.class);
        when(messageMock.getOrder()).thenReturn(order);
        when(messageMock.getType()).thenReturn(type);
        when(messageMock.getReasons()).thenReturn(reasons);

        return messageMock;
    }

    private final void coverageOnEnumsCorrection() {
        CurrencyCode
            .valueOf(CurrencyCode.EUR.toString());
        StrategyRunState
            .valueOf(StrategyRunState.STARTED.toString());
        OrderCallReason
            .valueOf(OrderCallReason.CHANGE_AMOUNT.toString());
        ConnectionState
            .valueOf(ConnectionState.CONNECTED.toString());
        LoginState
            .valueOf(LoginState.LOGGED_IN.toString());
        OrderProcessState
            .valueOf(OrderProcessState.ACTIVE.toString());
        OrderEventType
            .valueOf(OrderEventType.SUBMIT_OK.toString());
        OrderDirection
            .valueOf(OrderDirection.FLAT.toString());
        AuthentificationUtil.FSMTrigger
            .valueOf(AuthentificationUtil.FSMTrigger.CONNECT.toString());
    }

    protected final Completable emptyCompletable() {
        return Completable.complete();
    }

    protected final Completable neverCompletable() {
        return Completable.never();
    }

    protected final Observable<OrderEvent> emptyObservable() {
        return Observable.empty();
    }

    protected final Observable<OrderEvent> neverObservable() {
        return Observable.never();
    }

    protected final Observable<OrderEvent> eventObservable(final OrderEvent orderEvent) {
        return Observable.just(orderEvent);
    }

    protected final Observable<OrderEvent> eventObservable(final IOrder order,
                                                           final OrderEventType type) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     type,
                                                     true);
        return eventObservable(orderEvent);
    }

    @SuppressWarnings("unchecked")
    protected final <T> T getOnNextEvent(final TestObserver<T> observer,
                                         final int index) {
        return (T) observer.getEvents().get(0).get(index);
    }

    protected final <T> void testEqualsContract(final T instance) {
        EqualsVerifier
            .forClass(instance.getClass())
            .verify();

        logger.info("toString() for " + instance.toString());
    }
}
