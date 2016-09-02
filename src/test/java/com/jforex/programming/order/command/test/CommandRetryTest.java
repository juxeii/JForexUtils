package com.jforex.programming.order.command.test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.command.CommandRetry;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class CommandRetryTest extends CommonUtilForTest {

    private CommandRetry processRetry;

    @Mock
    private Callable<IOrder> callableMock;
    private final int noOfRetries = 3;
    private final long delayInMillis = 1500L;

    @Before
    public void SetAmountProcess() {
        processRetry = new CommandRetry(noOfRetries, delayInMillis);
    }

    public class RetryObservableSetup {

        private final OrderEvent rejectEvent = new OrderEvent(buyOrderEURUSD, OrderEventType.CLOSE_REJECTED);
        private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
        private final OrderCallRejectException rejectException =
                new OrderCallRejectException("Reject exception for test", rejectEvent);
        private Runnable retryCall;

        private final long delayOnOrderFailRetry = userSettings.delayOnOrderFailRetry();
        private final int maxRetriesOnOrderFail = userSettings.maxRetriesOnOrderFail();

        @Before
        public void setUp() {
            retryCall = () -> Observable
                .fromCallable(callableMock)
                .retryWhen(processRetry::retryOnRejectObservable)
                .subscribe(orderSubscriber);
        }

        @Test
        public void retryObservableErrorsForNonRejectException() throws Exception {
            when(callableMock.call()).thenThrow(jfException);

            retryCall.run();

            orderSubscriber.assertError(JFException.class);
        }

        public class SequenceSetup {

            private void expectAllRetriesThenSuccess() throws Exception {
                final Throwable throwables[] = new Throwable[maxRetriesOnOrderFail];
                for (int i = 0; i < maxRetriesOnOrderFail; ++i) {
                    throwables[i] = rejectException;
                }

                when(callableMock.call())
                    .thenThrow(throwables)
                    .thenReturn(buyOrderEURUSD);
            }

            private void exceeAllRetries() throws Exception {
                final Throwable throwables[] = new Throwable[maxRetriesOnOrderFail + 1];
                for (int i = 0; i <= maxRetriesOnOrderFail; ++i) {
                    throwables[i] = rejectException;
                }

                when(callableMock.call())
                    .thenThrow(throwables);
            }

            @Test
            public void testThatAllRetriesAreDone() throws Exception {
                expectAllRetriesThenSuccess();

                retryCall.run();

                RxTestUtil.advanceTimeBy(maxRetriesOnOrderFail * delayOnOrderFailRetry,
                                         TimeUnit.MILLISECONDS);

                verify(callableMock, times(maxRetriesOnOrderFail + 1)).call();

                orderSubscriber.assertNoErrors();
                orderSubscriber.assertValueCount(1);
                orderSubscriber.assertCompleted();
            }

            @Test
            public void testThatAfterAllRetriesTheSubscriberErrors() throws Exception {
                exceeAllRetries();

                retryCall.run();

                RxTestUtil.advanceTimeBy(maxRetriesOnOrderFail * delayOnOrderFailRetry,
                                         TimeUnit.MILLISECONDS);

                verify(callableMock, times(maxRetriesOnOrderFail + 1)).call();

                orderSubscriber.assertError(OrderCallRejectException.class);
            }
        }
    }
}
