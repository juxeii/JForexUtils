package com.jforex.programming.misc;

import java.util.Set;

import com.google.common.collect.Sets;

import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;

public final class JFHotSubject<T> {

    private final ConnectableObservable<T> hotObservable;
    private final Set<Subscriber<? super T>> subscribers = Sets.newConcurrentHashSet();

    public JFHotSubject() {
        final Observable<T> coldObservable = Observable.create(subscriber -> subscribe(subscriber));
        hotObservable = coldObservable.publish();
        hotObservable.connect();
    }

    public final Observable<T> observable() {
        return hotObservable;
    }

    public final void onNext(final T observableInstance) {
        subscribers.forEach(subscriber -> subscriber.onNext(observableInstance));
    }

    private final void subscribe(final Subscriber<? super T> subscriber) {
        subscriber.add(Subscriptions.create(() -> subscribers.remove(subscriber)));
        subscribers.add(subscriber);
    }
}
