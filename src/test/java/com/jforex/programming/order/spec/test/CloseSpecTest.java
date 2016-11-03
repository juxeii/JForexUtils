package com.jforex.programming.order.spec.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.spec.CloseSpec;
import com.jforex.programming.order.spec.CloseSpec.Builder;
import com.jforex.programming.order.spec.OrderEventConsumer;
import com.jforex.programming.test.common.QuoteProviderForTest;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class CloseSpecTest extends QuoteProviderForTest {

    private Builder builder;

    @Mock
    private OrderEventConsumer closeMock;
    @Mock
    private OrderEventConsumer closeRejectMock;
    private final Subject<OrderEvent> subject = PublishSubject.create();

    @Before
    public void setUp() {
        builder = CloseSpec
            .closeSpec(subject)
            .doOnClose(closeMock)
            .doOnReject(closeRejectMock);
    }

    @Test
    public void notSubscribedWhenNotStarted() {
        subject.onNext(closeEvent);

        verifyZeroInteractions(closeMock);
    }

    @Test
    public void whenStartedCloseHandlerIsCalled() {
        builder.start();

        subject.onNext(closeEvent);

        verify(closeMock).accept(closeEvent);
    }

    @Test
    public void whenStartedCloseRejectHandlerIsCalled() {
        builder.start();

        subject.onNext(closeRejectEvent);

        verify(closeRejectMock).accept(closeRejectEvent);
    }
}
