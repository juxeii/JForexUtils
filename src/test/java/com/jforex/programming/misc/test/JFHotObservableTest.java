package com.jforex.programming.misc.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class JFHotObservableTest extends CommonUtilForTest {

    private JFHotObservable<Integer> jfHotObservable;

    private TestObserver<Integer> testObserver;

    @Before
    public void setUp() throws Exception {
        jfHotObservable = new JFHotObservable<>();
    }

    @Test
    public void observableIsValid() {
        assertNotNull(jfHotObservable.observable());
    }

    public class ThreeItemsPublishedBeforeSubscribed {

        @Before
        public void setUp() {
            jfHotObservable.onNext(1);
            jfHotObservable.onNext(2);
            jfHotObservable.onNext(3);
        }

        public class WhenSubscribed {

            @Before
            public void setUp() throws Exception {
                testObserver = jfHotObservable
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
                    jfHotObservable.onNext(4);
                    jfHotObservable.onNext(5);
                }

                @Test
                public void itemsAreEmitted() {
                    testObserver.assertValueCount(2);
                    testObserver.assertValues(4, 5);
                }

                @Test
                public void afterUnsubscribeNoMoreItemsAreEmitted() {
                    jfHotObservable.unsubscribe();

                    jfHotObservable.onNext(6);

                    testObserver.assertValueCount(2);
                    testObserver.assertValues(4, 5);
                    testObserver.assertNotComplete();
                }
            }
        }
    }
}
