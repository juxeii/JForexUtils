package com.jforex.programming.order.command.test;

import java.util.Map;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SetLabelCommandTest extends CommonUtilForTest {

    private SetLabelCommand command;

    @Mock private Consumer<Throwable> errorActionMock;
    @Mock private Consumer<IOrder> doneActionMock;
    @Mock private Consumer<IOrder> rejectedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_LABEL));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final String newLabel = "newLabel";

//    @Before
//    public void SetLabelProcess() {
//        command = SetLabelCommand
//            .create(buyOrderEURUSD, newLabel, observable)
//            .onError(errorActionMock)
//            .onLabelChange(doneActionMock)
//            .onLabelReject(rejectedActionMock)
//            .doRetries(3, 1500L)
//            .build();
//
//        eventHandlerForType = command.eventHandlerForType();
//    }
//
//    @Test
//    public void emptyProcessHasNoRetriesAndActions() {
//        final SetLabelCommand emptyProcess = SetLabelCommand
//            .create(buyOrderEURUSD, newLabel, observable)
//            .build();
//
//        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();
//
//        assertThat(emptyProcess.noOfRetries(), equalTo(0));
//        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
//        assertTrue(eventHandlerForType.isEmpty());
//    }
//
//    @Test
//    public void processValuesAreCorrect() {
//        assertThat(command.errorAction(), equalTo(errorActionMock));
//        assertThat(command.order(), equalTo(buyOrderEURUSD));
//        assertThat(command.newLabel(), equalTo(newLabel));
//        assertThat(command.noOfRetries(), equalTo(3));
//        assertThat(command.delayInMillis(), equalTo(1500L));
//        assertThat(eventHandlerForType.size(), equalTo(2));
//    }
//
//    @Test
//    public void actionsAreCorrectMapped() {
//        eventHandlerForType.get(OrderEventType.CHANGE_LABEL_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGED_LABEL).accept(buyOrderEURUSD);
//
//        verify(doneActionMock).accept(buyOrderEURUSD);
//        verify(rejectedActionMock).accept(buyOrderEURUSD);
//    }
}
