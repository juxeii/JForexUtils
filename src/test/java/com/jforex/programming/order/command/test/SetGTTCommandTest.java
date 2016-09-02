package com.jforex.programming.order.command.test;

import java.util.Map;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SetGTTCommandTest extends CommonUtilForTest {

    private SetGTTCommand command;

    @Mock private Consumer<Throwable> errorActionMock;
    @Mock private Consumer<IOrder> doneActionMock;
    @Mock private Consumer<IOrder> rejectedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_GTT));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final long newGTT = 1L;

//    @Before
//    public void SetGTTProcess() {
//        command = SetGTTCommand
//            .create(buyOrderEURUSD, newGTT, observable)
//            .onError(errorActionMock)
//            .onGTTChange(doneActionMock)
//            .onGTTReject(rejectedActionMock)
//            .doRetries(3, 1500L)
//            .build();
//
//        eventHandlerForType = command.eventHandlerForType();
//    }
//
//    @Test
//    public void emptyProcessHasNoRetriesAndActions() {
//        final SetGTTCommand emptyProcess = SetGTTCommand
//            .create(buyOrderEURUSD, newGTT, observable)
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
//        assertThat(command.newGTT(), equalTo(newGTT));
//        assertThat(command.noOfRetries(), equalTo(3));
//        assertThat(command.delayInMillis(), equalTo(1500L));
//        assertThat(eventHandlerForType.size(), equalTo(2));
//    }
//
//    @Test
//    public void actionsAreCorrectMapped() {
//        eventHandlerForType.get(OrderEventType.CHANGE_GTT_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGED_GTT).accept(buyOrderEURUSD);
//
//        verify(doneActionMock).accept(buyOrderEURUSD);
//        verify(rejectedActionMock).accept(buyOrderEURUSD);
//    }
}
