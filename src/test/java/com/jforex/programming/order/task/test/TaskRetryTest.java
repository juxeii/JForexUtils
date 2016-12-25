package com.jforex.programming.order.task.test;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TaskRetryTest extends CommonUtilForTest {

    @Mock
    private Callable<IOrder> callableMock;
    private final Subject<OrderEvent> subject = PublishSubject.create();
    private TestObserver<OrderEvent> testObserver;
    private OrderEvent eventForTest;

    @Before
    public void setUp() {
        testObserver = TaskRetry
            .rejectObservable(subject, retryParams)
            .test();
    }

    private OrderEvent sendEvent(final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(buyOrderEURUSD,
                                                     orderEventType,
                                                     true);
        subject.onNext(orderEvent);
        return orderEvent;
    }

    private void advanceRetryTime() {
        RxTestUtil.advanceTimeInMillisBy(delayInMillis);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(TaskRetry.class);
    }

    @Test
    public void noRetryWhenNotARejectEvent() {
        eventForTest = sendEvent(OrderEventType.SUBMIT_OK);

        testObserver.assertNoErrors();
        testObserver.assertValue(eventForTest);
    }

    @Test
    public void noRetryWhenNotAnOrderCallRejectException() {
        subject.onError(jfException);

        testObserver.assertError(jfException);
    }

    public class OnCloseRejectEvent {

        @Before
        public void setUp() {
            sendEvent(OrderEventType.CLOSE_REJECTED);
        }

        @Test
        public void noValueEmitted() {
            testObserver.assertNoErrors();
            testObserver.assertNoValues();
        }

        public class OnSecondCloseRejectEvent {

            @Before
            public void setUp() {
                advanceRetryTime();

                sendEvent(OrderEventType.CLOSE_REJECTED);
            }

            @Test
            public void noValueEmitted() {
                testObserver.assertNoErrors();
                testObserver.assertNoValues();
            }

            @Test
            public void onThirdCloseRejectErrorIsEmitted() {
                advanceRetryTime();

                sendEvent(OrderEventType.CLOSE_REJECTED);

                testObserver.assertError(OrderCallRejectException.class);
            }

            @Test
            public void onCloseEventIsNowEmitted() {
                advanceRetryTime();

                eventForTest = sendEvent(OrderEventType.CLOSE_OK);

                testObserver.assertValue(eventForTest);
            }
        }
    }
}
