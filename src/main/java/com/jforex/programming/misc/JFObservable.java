package com.jforex.programming.misc;

import java.util.Set;

import com.google.common.collect.Sets;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public final class JFObservable<T> {

    private final Observable<T> observable = Observable.create(subscriber -> subscribe(subscriber));
    private final Set<Subscriber<? super T>> subscribers = Sets.newConcurrentHashSet();

    public final Observable<T> get() {
        return observable;
    }

    public final void onNext(final T observableInstance) {
        System.out.println("Number of subscribers for type " + observableInstance.getClass().toString() + ": "
                + subscribers.size());
        subscribers.forEach(consumer -> consumer.onNext(observableInstance));
    }

    private final void subscribe(final Subscriber<? super T> subscriber) {
        subscriber.add(Subscriptions.create(() -> subscribers.remove(subscriber)));
        subscribers.add(subscriber);
    }
}
