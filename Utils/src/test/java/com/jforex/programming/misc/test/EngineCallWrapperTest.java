package com.jforex.programming.misc.test;

import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.misc.EngineCallWrapper;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCall;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class EngineCallWrapperTest extends CommonUtilForTest {

    private EngineCallWrapper engineCallWrapper;

    private OrderCall orderRunnable;
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final Set<IOrder> toMergeOrders = createSet(IOrderForTest.buyOrderEURUSD(),
                                                        IOrderForTest.sellOrderEURUSD());
    private final String mergeLabel = "MergeLabel";

    @Before
    public void setUp() {
        initCommonTestFramework();

        engineCallWrapper = new EngineCallWrapper(engineMock);
    }

    @Test
    public void testSubmitCallRoutesToIEngine() throws JFException {
        orderRunnable = engineCallWrapper.submit(orderParams);

        orderRunnable.run();

        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void testMergeCallRoutesToIEngine() throws JFException {
        orderRunnable = engineCallWrapper.merge(mergeLabel, toMergeOrders);

        orderRunnable.run();

        verify(engineMock).mergeOrders(mergeLabel, toMergeOrders);
    }
}
