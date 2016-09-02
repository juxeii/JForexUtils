package com.jforex.programming.order.command.test;

import java.util.Map;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SetOpenPriceCommandTest extends CommonUtilForTest {

    private SetOpenPriceCommand command;

    @Mock private Consumer<Throwable> errorActionMock;
    @Mock private Consumer<IOrder> doneActionMock;
    @Mock private Consumer<IOrder> rejectedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_PRICE));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final double newOpenPrice = 1.1234;

//    @Before
//    public void SetOpenPriceProcess() {
//        command = SetOpenPriceCommand
//            .create(buyOrderEURUSD, newOpenPrice, observable)
//            .onError(errorActionMock)
//            .onOpenPriceChange(doneActionMock)
//            .onOpenPriceReject(rejectedActionMock)
//            .doRetries(3, 1500L)
//            .build();
//
//        eventHandlerForType = command.eventHandlerForType();
//    }
//
//    @Test
//    public void emptyProcessHasNoRetriesAndActions() {
//        final SetOpenPriceCommand emptyProcess = SetOpenPriceCommand
//            .create(buyOrderEURUSD, newOpenPrice, observable)
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
//        assertThat(command.newOpenPrice(), equalTo(newOpenPrice));
//        assertThat(command.noOfRetries(), equalTo(3));
//        assertThat(command.delayInMillis(), equalTo(1500L));
//        assertThat(eventHandlerForType.size(), equalTo(2));
//    }
//
//    @Test
//    public void actionsAreCorrectMapped() {
//        eventHandlerForType.get(OrderEventType.CHANGE_PRICE_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGED_PRICE).accept(buyOrderEURUSD);
//
//        verify(doneActionMock).accept(buyOrderEURUSD);
//        verify(rejectedActionMock).accept(buyOrderEURUSD);
//    }
}
