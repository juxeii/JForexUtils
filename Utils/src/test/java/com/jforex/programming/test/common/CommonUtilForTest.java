package com.jforex.programming.test.common;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mock;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.test.fakes.IEngineForTest;
import com.jforex.programming.test.fakes.IMessageForTest;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class CommonUtilForTest {

    @Mock protected IContext contextMock;
    @Mock protected IEngine engineMock;
    @Mock protected IHistory historyMock;
    @Mock protected ConcurrentUtil concurrentUtilMock;
    protected IEngineForTest engineForTest;
    protected JFException jfException = new JFException("JFException for test");
    protected Optional<Exception> jfExceptionOpt = Optional.of(jfException);
    protected Optional<Exception> emptyJFExceptionOpt = Optional.empty();

    protected final static Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    protected void initCommonTestFramework() {
        initMocks(this);

        engineForTest = new IEngineForTest(engineMock, jfException);
    }

    protected IMessage someOrderMessage(final IOrder order) {
        return new IMessageForTest(order,
                                   IMessage.Type.ORDER_CHANGED_OK,
                                   createSet(IMessage.Reason.ORDER_CHANGED_AMOUNT));
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> createSet(final T... elements) {
        return Stream.of(elements).collect(Collectors.toSet());
    }

    public static void setStrategyThread() {
        setThreadName(pfs.STRATEGY_THREAD_PREFIX());
    }

    public static void setNotStrategyThread() {
        setThreadName("Not" + pfs.STRATEGY_THREAD_PREFIX());
    }

    public static void setThreadName(final String threadName) {
        Thread.currentThread().setName(threadName);
    }
}
