package com.jforex.programming.order.command.test;

import java.util.Map;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SubmitCommandTest extends CommonUtilForTest {

    private SubmitCommand command;

    @Mock private Consumer<Throwable> errorActionMock;
    @Mock private Consumer<IOrder> submitRejectActionMock;
    @Mock private Consumer<IOrder> fillRejectActionMock;
    @Mock private Consumer<IOrder> partialFillActionMock;
    @Mock private Consumer<IOrder> submittedActionMock;
    @Mock private Consumer<IOrder> filledActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.FULLY_FILLED));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

//    @Before
//    public void setUp() {
//        command = SubmitCommand
//            .create(buyParamsEURUSD, observable)
//            .onError(errorActionMock)
//            .onSubmitReject(submitRejectActionMock)
//            .onFillReject(fillRejectActionMock)
//            .onSubmitOK(submittedActionMock)
//            .onPartialFill(partialFillActionMock)
//            .onFill(filledActionMock)
//            .doRetries(3, 1500L)
//            .build();
//
//        eventHandlerForType = command.eventHandlerForType();
//    }
//
//    @Test
//    public void emptyProcessHasNoRetriesAndActions() {
//        final SubmitCommand emptyCommand = SubmitCommand
//            .create(buyParamsEURUSD, observable)
//            .build();
//
//        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyCommand.eventHandlerForType();
//
//        assertThat(emptyCommand.noOfRetries(), equalTo(0));
//        assertThat(emptyCommand.delayInMillis(), equalTo(0L));
//        assertTrue(eventHandlerForType.isEmpty());
//    }
//
//    @Test
//    public void processValuesAreCorrect() {
//        assertThat(command.errorAction(), equalTo(errorActionMock));
//        assertThat(command.orderParams(), equalTo(buyParamsEURUSD));
//        assertThat(command.noOfRetries(), equalTo(3));
//        assertThat(command.delayInMillis(), equalTo(1500L));
//        assertThat(eventHandlerForType.size(), equalTo(6));
//    }
//
//    @Test
//    public void actionsAreCorrectMapped() {
//        eventHandlerForType.get(OrderEventType.SUBMIT_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.FILL_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.SUBMIT_OK).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.SUBMIT_CONDITIONAL_OK).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.PARTIAL_FILL_OK).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.FULLY_FILLED).accept(buyOrderEURUSD);
//
//        verify(submitRejectActionMock).accept(buyOrderEURUSD);
//        verify(fillRejectActionMock).accept(buyOrderEURUSD);
//        verify(submittedActionMock, times(2)).accept(buyOrderEURUSD);
//        verify(partialFillActionMock).accept(buyOrderEURUSD);
//        verify(filledActionMock).accept(buyOrderEURUSD);
//    }
}
