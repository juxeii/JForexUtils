package com.jforex.programming.misc;

import java.util.Set;

import com.google.common.collect.Sets;

import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.subscribers.DisposableSubscriber;

public final class JFHotSubject<T> {

    private final ConnectableFlowable<T> hotObservable;
    private final Set<DisposableSubscriber<? super T>> subscribers = Sets.newConcurrentHashSet();

    public JFHotSubject() {
        final Flowable<T> coldObservable =
                Flowable.unsafeCreate(subscriber -> subscribers.add((DisposableSubscriber<? super T>) subscriber));
        hotObservable = coldObservable.publish();
        hotObservable.connect();
    }

    public final Flowable<T> flowable() {
        return hotObservable;
    }

    public final void onNext(final T observableInstance) {
        subscribers.forEach(subscriber -> subscriber.onNext(observableInstance));
    }

    public final void unsubscribe() {
        // subscribers.forEach(Subscriber::unsubscribe);
    }
}
