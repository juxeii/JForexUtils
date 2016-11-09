package com.jforex.programming.order.task.params.test;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TaskParamsUtilTest extends InstrumentUtilForTest {

    private TaskParamsUtil taskParamsUtil;

    @Mock
    private Action startActionMock;
    @Mock
    private Action completeActionMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private Consumer<OrderEvent> consumerMockA;
    @Mock
    private Consumer<OrderEvent> consumerMockB;
    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    @Before
    public void setUp() {
        taskParamsUtil = new TaskParamsUtil();
    }

    @Test
    public void subscribeBasicParamsNoRetryDispatchesRejectEvent() throws Exception {
        final CloseParams closeParams = CloseParams
            .closeOrder(buyOrderEURUSD)
            .doOnReject(consumerMockB)
            .build();

        taskParamsUtil.subscribeBasicParams(orderEventSubject, closeParams);
        orderEventSubject.onNext(closeRejectEvent);

        verify(consumerMockB).accept(closeRejectEvent);
    }

    public class SubscribeBasicParamsWithRetry {

        @Before
        public void setUp() {
            final CloseParams closeParams = CloseParams
                .closeOrder(buyOrderEURUSD)
                .doOnStart(startActionMock)
                .doOnComplete(completeActionMock)
                .doOnError(errorConsumerMock)
                .doOnClose(consumerMockA)
                .doOnReject(consumerMockB)
                .retryOnReject(noOfRetries, delayInMillis)
                .build();

            taskParamsUtil.subscribeBasicParams(orderEventSubject, closeParams);
        }

        @Test
        public void startActionIsCalled() throws Exception {
            verify(startActionMock).run();
        }

        @Test
        public void completeActionIsCalledWhenCompleted() throws Exception {
            orderEventSubject.onComplete();
            verify(completeActionMock).run();
        }

        @Test
        public void errorConsumerIsCalledOnError() {
            orderEventSubject.onError(jfException);
            verify(errorConsumerMock).accept(jfException);
        }

        @Test
        public void closeEventIsDispatched() {
            orderEventSubject.onNext(closeEvent);
            verify(consumerMockA).accept(closeEvent);
        }

        @Test
        public void rejectEventIsNotDispatchedSinceRetryIsDefined() {
            orderEventSubject.onNext(closeRejectEvent);
            verifyZeroInteractions(consumerMockB);
        }

        @Test
        public void retryIsEstablished() throws Exception {
            orderEventSubject.onNext(closeRejectEvent);
            verify(startActionMock, timeout(2)).run();
        }
    }
}
