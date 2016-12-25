package com.jforex.programming.rx;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;

public final class JFHotPublisher<T> {

    private final PublishSubject<T> publisher = PublishSubject.create();
    private final ConnectableObservable<T> connectableObservable = publisher.publish();
    private final Disposable disposable = connectableObservable.connect();

    public final Observable<T> observable() {
        return connectableObservable;
    }

    public final void onNext(final T observableInstance) {
        publisher.onNext(observableInstance);
    }

    public final void unsubscribe() {
        disposable.dispose();
    }
}
