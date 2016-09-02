package com.jforex.programming.order.command.test;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.command.SimpleMergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class MergeCommandTest extends CommonUtilForTest {

    private SimpleMergeCommand command;

    @Mock private Consumer<Throwable> errorActionMock;
    @Mock private Consumer<IOrder> removedSLActionMock;
    @Mock private Consumer<IOrder> removedTPActionMock;
    @Mock private Consumer<IOrder> removedSLRejectedActionMock;
    @Mock private Consumer<IOrder> removedTPRejectedActionMock;
    @Mock private Consumer<IOrder> mergeRejectActionMock;
    @Mock private Consumer<IOrder> mergedActionMock;
    @Mock private Consumer<IOrder> mergeClosedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.MERGE_OK));
    private final String mergeOrderLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

//    @Before
//    public void setUp() {
//        command = MergeCommand
//            .create(mergeOrderLabel, toMergeOrders, observable)
//            .onError(errorActionMock)
//            .onRemoveSL(removedSLActionMock)
//            .onRemoveTP(removedTPActionMock)
//            .onRemoveSLReject(removedSLRejectedActionMock)
//            .onRemoveTPReject(removedTPRejectedActionMock)
//            .onMergeReject(mergeRejectActionMock)
//            .onMerge(mergedActionMock)
//            .onMergeClose(mergeClosedActionMock)
//            .doRetries(3, 1500L)
//            .build();
//
//        eventHandlerForType = command.eventHandlerForType();
//    }
//
//    @Test
//    public void emptyProcessHasNoRetriesAndActions() {
//        final MergeCommand emptyProcess = MergeCommand
//            .create(mergeOrderLabel, toMergeOrders, observable)
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
//        assertThat(command.mergeOrderLabel(), equalTo(mergeOrderLabel));
//        assertThat(command.toMergeOrders(), equalTo(toMergeOrders));
//        assertThat(command.noOfRetries(), equalTo(3));
//        assertThat(command.delayInMillis(), equalTo(1500L));
//        assertThat(eventHandlerForType.size(), equalTo(7));
//    }
//
//    @Test
//    public void actionsAreCorrectMapped() {
//        eventHandlerForType.get(OrderEventType.CHANGED_SL).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGED_TP).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGE_SL_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.CHANGE_TP_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.MERGE_REJECTED).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.MERGE_OK).accept(buyOrderEURUSD);
//        eventHandlerForType.get(OrderEventType.MERGE_CLOSE_OK).accept(buyOrderEURUSD);
//
//        verify(removedSLActionMock).accept(buyOrderEURUSD);
//        verify(removedTPActionMock).accept(buyOrderEURUSD);
//        verify(removedSLRejectedActionMock).accept(buyOrderEURUSD);
//        verify(removedTPRejectedActionMock).accept(buyOrderEURUSD);
//        verify(mergeRejectActionMock).accept(buyOrderEURUSD);
//        verify(mergedActionMock).accept(buyOrderEURUSD);
//        verify(mergeClosedActionMock).accept(buyOrderEURUSD);
//    }
}
