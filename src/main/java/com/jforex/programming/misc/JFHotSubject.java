package com.jforex.programming.misc;

import java.util.Set;

import com.google.common.collect.Sets;

import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

public final class JFHotSubject<T> {

    private final ConnectableObservable<T> hotObservable;
    private final Set<Subscriber<? super T>> subscribers = Sets.newConcurrentHashSet();

    public JFHotSubject() {
        final Observable<T> coldObservable = Observable.create(subscriber -> subscribers.add(subscriber));
        hotObservable = coldObservable.publish();
        hotObservable.connect();
    }

    public final Observable<T> observable() {
        return hotObservable;
    }

    public final void onNext(final T observableInstance) {
        subscribers.forEach(subscriber -> subscriber.onNext(observableInstance));
    }

    public final void unsubscribe() {
        subscribers.forEach(Subscriber::unsubscribe);
    }
}
