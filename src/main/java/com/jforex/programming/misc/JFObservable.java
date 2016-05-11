package com.jforex.programming.misc;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.jforex.programming.position.PositionEvent;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public final class JFObservable<T> {

    private final Observable<T> observable = Observable.create(subscriber -> subscribe(subscriber));
    private final Set<Subscriber<? super T>> subscribers = Sets.newConcurrentHashSet();

    private static final Logger logger = LogManager.getLogger(JFObservable.class);

    public final Observable<T> get() {
        return observable;
    }

    public final void onNext(final T observableInstance) {
        if (observableInstance instanceof PositionEvent)
            logger.info("Number of subscribers for type " + observableInstance.getClass().toString() + ": "
                    + subscribers.size());
        subscribers.forEach(consumer -> consumer.onNext(observableInstance));
    }

    private final void subscribe(final Subscriber<? super T> subscriber) {
        subscriber.add(Subscriptions.create(() -> {
            subscribers.remove(subscriber);
            logger.info("REMOVING subscriber!");
        }));
        subscribers.add(subscriber);
    }
}
