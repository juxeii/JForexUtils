package com.jforex.programming.misc;

import java.util.Set;

import com.google.common.collect.Sets;

import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public final class JFEventPublisherForRx<EventType> {

    private final Set<Subscriber<? super EventType>> subscribers = Sets.newConcurrentHashSet();

    public final void onJFEvent(final EventType jfEvent) {
        subscribers.forEach(consumer -> consumer.onNext(jfEvent));
    }

    public final void subscribe(final Subscriber<? super EventType> subscriber) {
        subscriber.add(Subscriptions.create(() -> subscribers.remove(subscriber)));
        subscribers.add(subscriber);
    }
}
