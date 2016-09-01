package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.MergeProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class MergeProcessTest extends CommonUtilForTest {

    private MergeProcess process;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> removedSLActionMock;
    @Mock
    private Consumer<IOrder> removedTPActionMock;
    @Mock
    private Consumer<IOrder> removedSLRejectedActionMock;
    @Mock
    private Consumer<IOrder> removedTPRejectedActionMock;
    @Mock
    private Consumer<IOrder> mergeRejectActionMock;
    @Mock
    private Consumer<IOrder> mergedActionMock;
    @Mock
    private Consumer<IOrder> mergeClosedActionMock;
    private final String mergeOrderLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    @Before
    public void setUp() {
        process = MergeProcess
            .forParams(mergeOrderLabel, toMergeOrders)
            .onError(errorActionMock)
            .onRemoveSL(removedSLActionMock)
            .onRemoveTP(removedTPActionMock)
            .onRemoveSLReject(removedSLRejectedActionMock)
            .onRemoveTPReject(removedTPRejectedActionMock)
            .onMergeReject(mergeRejectActionMock)
            .onMerge(mergedActionMock)
            .onMergeClose(mergeClosedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = process.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final MergeProcess emptyProcess = MergeProcess
            .forParams(mergeOrderLabel, toMergeOrders)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(process.errorAction(), equalTo(errorActionMock));
        assertThat(process.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(process.toMergeOrders(), equalTo(toMergeOrders));
        assertThat(process.noOfRetries(), equalTo(3));
        assertThat(process.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(7));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CHANGED_SL).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGED_TP).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_SL_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_TP_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.MERGE_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.MERGE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.MERGE_CLOSE_OK).accept(buyOrderEURUSD);

        verify(removedSLActionMock).accept(buyOrderEURUSD);
        verify(removedTPActionMock).accept(buyOrderEURUSD);
        verify(removedSLRejectedActionMock).accept(buyOrderEURUSD);
        verify(removedTPRejectedActionMock).accept(buyOrderEURUSD);
        verify(mergeRejectActionMock).accept(buyOrderEURUSD);
        verify(mergedActionMock).accept(buyOrderEURUSD);
        verify(mergeClosedActionMock).accept(buyOrderEURUSD);
    }
}
