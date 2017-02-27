package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class PositionFactoryTest extends InstrumentUtilForTest {

    private PositionFactory positionFactory;

    private final Observable<OrderEvent> observable = eventObservable(buyOrderEURUSD, OrderEventType.SUBMIT_OK);

    @Before
    public void setUp() {
        positionFactory = new PositionFactory(observable);
    }

    @Test
    public void allPositionsIsEmptyAfterCreation() {
        assertTrue(positionFactory.all().isEmpty());
    }

    public class EURUSDRetreival {

        private Position positionEURUSD;

        @Before
        public void setUp() {
            positionEURUSD = positionFactory.forInstrument(instrumentEURUSD);
        }

        @Test
        public void positionIsForCorrectInstrument() {
            assertThat(positionEURUSD.instrument(), equalTo(instrumentEURUSD));
        }

        @Test
        public void allPositionsHasEURUSDPosition() {
            final List<Position> positions = Lists.newArrayList(positionFactory.all());

            assertThat(positions.size(), equalTo(1));
            assertTrue(positions.contains(positionEURUSD));
        }

        public class SecondEURUSDRetreival {

            @Before
            public void setUp() {
                positionFactory.forInstrument(instrumentEURUSD);
            }

            @Test
            public void positionIsForCorrectInstrument() {
                assertThat(positionEURUSD.instrument(), equalTo(instrumentEURUSD));
            }

            @Test
            public void allPositionsHasEURUSDPosition() {
                final List<Position> positions = Lists.newArrayList(positionFactory.all());

                assertThat(positions.size(), equalTo(1));
                assertTrue(positions.contains(positionEURUSD));
            }
        }

        public class AUDUSDRetreival {

            private Position positionAUDUSD;

            @Before
            public void setUp() {
                positionAUDUSD = positionFactory.forInstrument(instrumentAUDUSD);
            }

            @Test
            public void positionIsForCorrectInstrument() {
                assertThat(positionAUDUSD.instrument(), equalTo(instrumentAUDUSD));
            }

            @Test
            public void allPositionsHasEURUSDAndAUDUSDPosition() {
                final List<Position> positions = Lists.newArrayList(positionFactory.all());

                assertThat(positions.size(), equalTo(2));
                assertTrue(positions.contains(positionEURUSD));
                assertTrue(positions.contains(positionAUDUSD));
            }
        }
    }
}
