package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderTaskExecutorTest extends CommonUtilForTest {

    private OrderTaskExecutor orderTaskExecutor;

    @Mock
    private TaskExecutor taskExecutorMock;
    @Mock
    private IEngineUtil engineUtilMock;
    private final TestSubscriber<IOrder> orderSubscriber = TestSubscriber.create();
    private final Callable<IOrder> callable = () -> buyOrderEURUSD;

    @Before
    public void setUp() throws Exception {
        setUpMocks();

        orderTaskExecutor = new OrderTaskExecutor(taskExecutorMock, engineUtilMock);
    }

    private void setUpMocks() throws Exception {
        when(engineUtilMock.submitCallable(buyParamsEURUSD)).thenReturn(callable);
    }

    private void setUpTaskExecutorSingle(final Single<IOrder> orderSingle) {
        when(taskExecutorMock.onStrategyThread(callable)).thenReturn(orderSingle);
    }

    public class SubmitOrderSetup {

        private final Single<IOrder> expectedSingle = Single.just(buyOrderEURUSD);
        private Single<IOrder> returnedSingle;

        @Before
        public void setUp() {
            setUpTaskExecutorSingle(expectedSingle);

            returnedSingle = orderTaskExecutor.submitOrder(buyParamsEURUSD);
        }

        @Test
        public void submitOrderReturnsSingleFromTaskExecutor() {
            assertThat(returnedSingle, equalTo(expectedSingle));
        }

        @Test
        public void submitOrderReturnsCorrectOrderInstance() {
            final IOrder returnedOrder = returnedSingle.blockingGet();

            assertThat(returnedOrder, equalTo(buyOrderEURUSD));
        }
    }
}
