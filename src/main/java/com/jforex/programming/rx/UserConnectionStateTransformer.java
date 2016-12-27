package com.jforex.programming.rx;

import com.jforex.programming.connection.UserConnectionState;

import io.reactivex.ObservableTransformer;

public interface UserConnectionStateTransformer extends ObservableTransformer<UserConnectionState,
                                                                              UserConnectionState> {
}
