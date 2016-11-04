package com.jforex.programming.order.spec.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.spec.BasicSpec;
import com.jforex.programming.order.spec.ErrorConsumer;
import com.jforex.programming.order.spec.OrderEventConsumer;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.common.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BasicSpecTest extends QuoteProviderForTest {

    @Mock
    private OrderEventConsumer consumerMock;
    @Mock
    private Action actionMock;
    @Mock
    private ErrorConsumer errorConsumerMock;
    private final Subject<OrderEvent> eventSubject = PublishSubject.create();

    private void emitEventAndVerifyConsumerCall(final OrderEvent orderEvent) {
        eventSubject.onNext(orderEvent);

        verify(consumerMock).accept(orderEvent);
    }

    public class CommonTests {

        @Test
        public void whenNotStartedNoSubscription() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnSubmit(consumerMock);

            verifyZeroInteractions(consumerMock);
        }

        @Test
        public void noActionWhenEventHasNoConsumer() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnSubmit(consumerMock)
                .start();

            eventSubject.onNext(fillEvent);

            verifyZeroInteractions(consumerMock);
        }

        @Test
        public void doOnStartIsCalledOnSubscription() throws Exception {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnStart(actionMock)
                .start();

            verify(actionMock).run();
        }

        @Test
        public void doOnCompleteIsCalledOnCompletion() throws Exception {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnComplete(actionMock)
                .start();

            eventSubject.onComplete();

            verify(actionMock).run();
        }

        @Test
        public void doOnExceptionIsCalledOnError() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnException(errorConsumerMock)
                .start();

            eventSubject.onError(jfException);

            verify(errorConsumerMock).accept(jfException);
        }

        @Test
        public void retryIsComposedWhenSpecified() {
            final long retryDelay = 1500L;

            BasicSpec
                .forSubmit(eventSubject)
                .doOnFullFill(consumerMock)
                .retryOnReject(2, retryDelay)
                .start();

            eventSubject.onNext(fillRejectEvent);
            verifyZeroInteractions(consumerMock);
            RxTestUtil.advanceTimeInMillisBy(retryDelay);

            eventSubject.onNext(fillRejectEvent);
            verifyZeroInteractions(consumerMock);
            RxTestUtil.advanceTimeInMillisBy(retryDelay);

            emitEventAndVerifyConsumerCall(fillEvent);
        }
    }

    public class SubmitFixture {

        @Test
        public void submitHandlerIsCalled() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnSubmit(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(submitEvent);
        }

        @Test
        public void partialFillHandlerIsCalled() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnPartialFill(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(partialFillEvent);
        }

        @Test
        public void fillHandlerIsCalled() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnFullFill(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(fillEvent);
        }

        @Test
        public void submitRejectHandlerIsCalled() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnSubmitReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(submitRejectEvent);
        }

        @Test
        public void fillRejectHandlerIsCalled() {
            BasicSpec
                .forSubmit(eventSubject)
                .doOnFillReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(fillRejectEvent);
        }
    }

    public class MergeFixture {

        @Test
        public void mergeHandlerIsCalled() {
            BasicSpec
                .forMerge(eventSubject)
                .doOnMerge(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(mergeEvent);
        }

        @Test
        public void mergeCloseHandlerIsCalled() {
            BasicSpec
                .forMerge(eventSubject)
                .doOnMergeClose(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(mergeCloseEvent);
        }

        @Test
        public void mergeRejectHandlerIsCalled() {
            BasicSpec
                .forMerge(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(mergeRejectEvent);
        }
    }

    public class CloseFixture {

        @Test
        public void closeHandlerIsCalled() {
            BasicSpec
                .forClose(eventSubject)
                .doOnClose(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(closeEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forClose(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(closeRejectEvent);
        }
    }

    public class SetLabelFixture {

        @Test
        public void setLabelHandlerIsCalled() {
            BasicSpec
                .forSetLabel(eventSubject)
                .doOnChangedLabel(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedLabelEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetLabel(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(labelChangeRejectEvent);
        }
    }

    public class SetAmountFixture {

        @Test
        public void setAmountHandlerIsCalled() {
            BasicSpec
                .forSetAmount(eventSubject)
                .doOnChangedAmount(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedAmountEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetAmount(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(setAmountRejectEvent);
        }
    }

    public class SetGTTFixture {

        @Test
        public void setGTTHandlerIsCalled() {
            BasicSpec
                .forSetGTT(eventSubject)
                .doOnChangedGTT(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedGTTEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetGTT(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(setGTTRejectEvent);
        }
    }

    public class SetOpenPriceFixture {

        @Test
        public void setOpenPriceHandlerIsCalled() {
            BasicSpec
                .forSetOpenPrice(eventSubject)
                .doOnChangedOpenPrice(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedOpenPriceEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetOpenPrice(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(setOpenPriceRejectEvent);
        }
    }

    public class SetSLFixture {

        @Test
        public void setSLHandlerIsCalled() {
            BasicSpec
                .forSetSL(eventSubject)
                .doOnChangedSL(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedSLEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetSL(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(setSLRejectEvent);
        }
    }

    public class SetTPFixture {

        @Test
        public void setTPHandlerIsCalled() {
            BasicSpec
                .forSetTP(eventSubject)
                .doOnChangedTP(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(changedTPEvent);
        }

        @Test
        public void rejectHandlerIsCalled() {
            BasicSpec
                .forSetTP(eventSubject)
                .doOnReject(consumerMock)
                .start();

            emitEventAndVerifyConsumerCall(setTPRejectEvent);
        }
    }
}
