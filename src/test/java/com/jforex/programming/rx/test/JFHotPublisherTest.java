package com.jforex.programming.rx.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class JFHotPublisherTest extends CommonUtilForTest {

    private JFHotPublisher<Integer> jfHotPublisher;

    private TestObserver<Integer> testObserver;

    @Before
    public void setUp() throws Exception {
        jfHotPublisher = new JFHotPublisher<>();
    }

    @Test
    public void observableIsValid() {
        assertNotNull(jfHotPublisher.observable());
    }

    public class ThreeItemsPublishedBeforeSubscribed {

        @Before
        public void setUp() {
            jfHotPublisher.onNext(1);
            jfHotPublisher.onNext(2);
            jfHotPublisher.onNext(3);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() throws Exception {
                testObserver = jfHotPublisher
                    .observable()
                    .test();
            }

            @Test
            public void noItemsAreEmittedSinceThisIsAHotObservable() {
                testObserver.assertNoValues();
            }

            public class TwoItemsEmittedAfterSubscription {

                @Before
                public void setUp() throws Exception {
                    jfHotPublisher.onNext(4);
                    jfHotPublisher.onNext(5);
                }

                @Test
                public void itemsAreEmitted() {
                    testObserver.assertValueCount(2);
                    testObserver.assertValues(4, 5);
                }

                @Test
                public void afterUnsubscribeNoMoreItemsAreEmitted() {
                    jfHotPublisher.unsubscribe();

                    jfHotPublisher.onNext(6);

                    testObserver.assertValueCount(2);
                    testObserver.assertValues(4, 5);
                    testObserver.assertNotComplete();
                }
            }
        }
    }
}
