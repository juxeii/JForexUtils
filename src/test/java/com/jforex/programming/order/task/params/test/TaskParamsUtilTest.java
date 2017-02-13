package com.jforex.programming.order.task.params.test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;
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
    @Mock
    private ClosePositionParams closePositionParamsMock;
    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();
    private final ComposeDataImpl composeDataImpl = new ComposeDataImpl();
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();

    @Before
    public void setUp() {
        when(closePositionParamsMock.composeData())
            .thenReturn(composeDataImpl);

        taskParamsUtil = spy(new TaskParamsUtil());
    }

    @Test
    public void subscribeBasicParamsNoRetryIsDoneWhenNotSpecified() throws Exception {
        final CloseParams closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .doOnStart(startActionMock)
            .build();

        taskParamsUtil.composeAndSubscribe(orderEventSubject, closeParams);
        orderEventSubject.onNext(closeRejectEvent);

        verify(startActionMock).run();
    }

    public class SubscribeBasicParams {

        @Before
        public void setUp() {
            final CloseParams closeParams = CloseParams
                .withOrder(buyOrderEURUSD)
                .doOnStart(startActionMock)
                .doOnComplete(completeActionMock)
                .doOnError(errorConsumerMock)
                .doOnClose(consumerMockA)
                .doOnReject(consumerMockB)
                .retryOnReject(retryParams)
                .build();

            taskParamsUtil.composeAndSubscribe(orderEventSubject, closeParams);
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
        public void rejectEventIsDispatched() {
            orderEventSubject.onNext(closeRejectEvent);
            verify(consumerMockB).accept(closeRejectEvent);
        }
    }

    public class ComposeParamsTests {

        private TestObserver<OrderEvent> testObserver;

        @Before
        public void setUp() {
            composeDataImpl.setStartAction(startActionMock);
            composeDataImpl.setCompleteAction(completeActionMock);
            composeDataImpl.setErrorConsumer(errorConsumerMock);
            composeDataImpl.setRetryParams(retryParams);

            when(closePositionParamsMock.composeData())
                .thenReturn(composeDataImpl);

            testObserver = taskParamsUtil
                .compose(orderEventSubject, closePositionParamsMock)
                .test();
        }

        @Test
        public void startActionIsCalled() throws Exception {
            verify(startActionMock).run();
            testObserver.assertSubscribed();
        }

        @Test
        public void completeActionIsCalledWhenCompleted() throws Exception {
            orderEventSubject.onComplete();
            verify(completeActionMock).run();
            testObserver.assertComplete();
        }

        @Test
        public void errorConsumerIsCalledOnError() {
            orderEventSubject.onError(jfException);
            verify(errorConsumerMock).accept(jfException);
            testObserver.assertError(jfException);
        }
    }

    public class ComposeParamsForOrderTests {

        private TestObserver<OrderEvent> testObserver;

        @Before
        public void setUp() {
            consumerForEvent.put(OrderEventType.CLOSE_OK, consumerMockA);
            composeDataImpl.setStartAction(startActionMock);
            composeDataImpl.setCompleteAction(completeActionMock);
            composeDataImpl.setErrorConsumer(errorConsumerMock);
            composeDataImpl.setRetryParams(retryParams);

            when(closePositionParamsMock.composeData())
                .thenReturn(composeDataImpl);

            testObserver = taskParamsUtil
                .compose(orderEventSubject, closePositionParamsMock)
                .test();
        }

        @Test
        public void startActionIsCalled() throws Exception {
            verify(startActionMock).run();
            testObserver.assertSubscribed();
        }

        @Test
        public void completeActionIsCalledWhenCompleted() throws Exception {
            orderEventSubject.onComplete();
            verify(completeActionMock).run();
            testObserver.assertComplete();
        }

        @Test
        public void errorConsumerIsCalledOnError() {
            orderEventSubject.onError(jfException);
            verify(errorConsumerMock).accept(jfException);
            testObserver.assertError(jfException);
        }
    }
}
