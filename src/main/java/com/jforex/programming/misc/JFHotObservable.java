package com.jforex.programming.misc;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;

public final class JFHotObservable<T> {

    private final PublishSubject<T> source = PublishSubject.create();
    private final ConnectableObservable<T> connectableObservable = source.publish();
    private final Disposable disposable = connectableObservable.connect();

    public final Observable<T> observable() {
        return connectableObservable;
    }

    public final void onNext(final T observableInstance) {
        source.onNext(observableInstance);
    }

    public final void unsubscribe() {
        disposable.dispose();
    }
}
