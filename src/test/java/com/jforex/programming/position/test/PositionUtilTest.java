package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

    private PositionUtil positionUtil;

    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private TestObserver<OrderEvent> testSubscriber;

    @Before
    public void setUp() {
        setUpMocks();

        positionUtil = new PositionUtil(orderUtilMock, positionFactoryMock);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private void setUpOrderUtilObservables(final Collection<IOrder> toMergeOrders,
                                           final Observable<OrderEvent> observable) {
        when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(observable);
    }

    @Test
    public void positionOrdersIsCorrect() {
        assertThat(positionUtil.positionOrders(instrumentEURUSD), equalTo(positionMock));
    }

    public class MergePositionTests {

        private Observable<OrderEvent> mergeObservable;

        @Before
        public void setUp() {
            mergeObservable = positionUtil.merge(instrumentEURUSD, mergeOrderLabel);
        }

        private void expectFilledOrders(final Set<IOrder> filledOrders) {
            when(positionMock.filled()).thenReturn(filledOrders);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderUtilMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        public class OnSubscribe {

            public void prepareToMergeOrdersAndSubscribe(final Set<IOrder> toMergeOrders) {
                expectFilledOrders(toMergeOrders);
                setUpOrderUtilObservables(toMergeOrders, emptyObservable());

                testSubscriber = mergeObservable.test();
            }

            @Test
            public void completesImmediatelyWhenNoOrdersForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet());

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void completesImmediatelyWhenOnlyOneOrderForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet(buyOrderEURUSD));

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void callOnOrderUtilWhenEnoughOrdersForMerge() {
                final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
                prepareToMergeOrdersAndSubscribe(toMergeOrders);

                testSubscriber.assertComplete();
                verify(orderUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }
        }
    }
}
