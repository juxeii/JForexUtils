package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class StreamUtilTest extends CommonUtilForTest {

    @Mock
    private Callable<IOrder> callableMock;

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(StreamUtil.class);
    }

    @Test
    public void waitObservableIsCorrect() {
        final TestObserver<Long> subscriber = TestObserver.create();

        StreamUtil.waitObservable(1000L, TimeUnit.MILLISECONDS).subscribe(subscriber);

        RxTestUtil.advanceTimeBy(900L, TimeUnit.MILLISECONDS);
        subscriber.assertNotComplete();
        RxTestUtil.advanceTimeBy(100L, TimeUnit.MILLISECONDS);
        subscriber.assertComplete();
    }

    @Test
    public void streamOptionalReturnsEmptyStreamWhenOptionalIsEmpty() {
        final Optional<?> emptyOptional = Optional.empty();

        final Stream<?> streamOptional = StreamUtil.optionalStream(emptyOptional);

        assertThat(streamOptional.count(), equalTo(0L));
    }

    @Test
    public void streamOptionalReturnsStreamOfOptionalInstance() {
        final Optional<Long> optional = Optional.of(1L);

        final List<Long> streamList = StreamUtil
            .optionalStream(optional)
            .collect(Collectors.toList());

        assertThat(streamList.size(), equalTo(1));
        assertThat(streamList.get(0), equalTo(1L));
    }
}
