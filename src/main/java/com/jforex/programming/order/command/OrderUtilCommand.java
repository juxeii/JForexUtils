package com.jforex.programming.order.command;

import rx.Completable;

public interface OrderUtilCommand {

    public void start();

    public Completable completable();
}
