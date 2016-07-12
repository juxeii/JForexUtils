package com.jforex.programming.test.common;

import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.test.fakes.IClientForTest;
import com.jforex.programming.test.fakes.IEngineForTest;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected JForexUtil jforexUtilMock;
    @Mock
    protected IClient clientMock;
    @Mock
    protected IContext contextMock;
    @Mock
    protected IEngine engineMock;
    @Mock
    protected IHistory historyMock;
    @Mock
    protected HistoryUtil historyUtilMock;
    protected IClientForTest clientForTest;
    protected IEngineForTest engineForTest;
    protected JFException jfException = new JFException("JFException for test");
    protected Optional<Exception> jfExceptionOpt = Optional.of(jfException);
    protected Optional<Exception> emptyJFExceptionOpt = Optional.empty();
    protected final RxTestUtil rxTestUtil = RxTestUtil.get();

    protected final static PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);
    protected final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    protected final static Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    protected void initCommonTestFramework() {
        initMocks(this);

        clientForTest = new IClientForTest(clientMock);
        engineForTest = new IEngineForTest(engineMock, jfException);

        when(jforexUtilMock.context()).thenReturn(contextMock);
        when(jforexUtilMock.history()).thenReturn(historyMock);
        when(jforexUtilMock.historyUtil()).thenReturn(historyUtilMock);

        coverageOnEnumsCorrection();
    }

    public static void setStrategyThread() {
        setThreadName(platformSettings.strategyThreadPrefix());
    }

    public static void setNotStrategyThread() {
        setThreadName("Not" + platformSettings.strategyThreadPrefix());
    }

    public static void setThreadName(final String threadName) {
        Thread.currentThread().setName(threadName);
    }

    private void coverageOnEnumsCorrection() {
        StrategyRunState.valueOf(StrategyRunState.STARTED.toString());
        OrderCallReason.valueOf(OrderCallReason.CHANGE_AMOUNT.toString());
        ConnectionState.valueOf(ConnectionState.CONNECTED.toString());
        LoginState.valueOf(LoginState.LOGGED_IN.toString());
    }
}
