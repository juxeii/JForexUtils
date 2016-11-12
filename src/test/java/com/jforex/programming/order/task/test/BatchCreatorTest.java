package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchCreator;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BatchCreatorTest extends InstrumentUtilForTest {

    private BatchCreator batchCreator;

    @Mock
    private Function<IOrder, Observable<OrderEvent>> composerFunctionMock;
    private TestObserver<OrderEvent> testObserver;
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        batchCreator = new BatchCreator();
    }

    private void setupFunctionMock(final Observable<OrderEvent> buyObservable,
                                   final Observable<OrderEvent> sellObservable) {
        when(composerFunctionMock.apply(buyOrderEURUSD))
            .thenReturn(buyObservable);
        when(composerFunctionMock.apply(sellOrderEURUSD))
            .thenReturn(sellObservable);
    }

    private void subscribeWithBatchMode(final BatchMode batchMode) {
        testObserver = batchCreator
            .create(ordersForBatch,
                    batchMode,
                    composerFunctionMock)
            .test();
    }

    @Test
    public void eventsForMergeAreReceivedInAnyOrder() {
        setupFunctionMock(eventObservable(closeEvent), eventObservable(closeRejectEvent));

        subscribeWithBatchMode(BatchMode.MERGE);

        testObserver.assertComplete();
        testObserver.assertValueSet(Sets.newHashSet(closeEvent, closeRejectEvent));
    }

    @Test
    public void eventsForConcatAreReceivedInOrder() {
        setupFunctionMock(eventObservable(closeEvent), eventObservable(closeRejectEvent));

        subscribeWithBatchMode(BatchMode.CONCAT);

        testObserver.assertComplete();
        testObserver.assertValues(closeEvent, closeRejectEvent);
    }

    @Test
    public void concatModeIsCorrect() {
        setupFunctionMock(neverObservable(), eventObservable(closeRejectEvent));

        subscribeWithBatchMode(BatchMode.CONCAT);

        testObserver.assertNotComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void mergeModeIsCorrect() {
        setupFunctionMock(neverObservable(), eventObservable(closeRejectEvent));

        subscribeWithBatchMode(BatchMode.MERGE);

        testObserver.assertNotComplete();
        testObserver.assertValue(closeRejectEvent);
    }
}
