package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;
import rx.functions.Action0;

public class CloseCommandTest extends CommonUtilForTest {

    private CloseCommand comamnd;

    @Mock
    private Action0 completedActionMock;
    @Mock
    private Consumer<OrderEvent> eventActionMock;
    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> closeRejectActionMock;
    @Mock
    private Consumer<IOrder> closedActionMock;
    @Mock
    private Consumer<IOrder> partialClosedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CLOSE_OK));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    @Before
    public void setUp() {
        comamnd = CloseCommand
            .create(buyOrderEURUSD, observable)
            .onError(errorActionMock)
            .onClose(closedActionMock)
            .onCloseReject(closeRejectActionMock)
            .onPartialClose(partialClosedActionMock)
            .onCompleted(completedActionMock)
            .onEvent(eventActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = comamnd.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final CloseCommand emptyProcess = CloseCommand
            .create(buyOrderEURUSD, observable)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        emptyProcess.completedAction().call();
        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(comamnd.completedAction(), equalTo(completedActionMock));
        assertThat(comamnd.eventAction(), equalTo(eventActionMock));
        assertThat(comamnd.errorAction(), equalTo(errorActionMock));
        assertThat(comamnd.orderToClose(), equalTo(buyOrderEURUSD));
        assertThat(comamnd.noOfRetries(), equalTo(3));
        assertThat(comamnd.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(3));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CLOSE_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CLOSE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.PARTIAL_CLOSE_OK).accept(buyOrderEURUSD);

        verify(closeRejectActionMock).accept(buyOrderEURUSD);
        verify(closedActionMock).accept(buyOrderEURUSD);
        verify(partialClosedActionMock).accept(buyOrderEURUSD);
    }
}
