package com.jforex.programming.connection.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.connection.UserConnection;
import com.jforex.programming.connection.UserConnectionState;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class UserConnectionTest extends CommonUtilForTest {

    private UserConnection userConnection;

    private TestObserver<UserConnectionState> userObservable;
    private final Subject<ConnectionState> connectionStateSubject = PublishSubject.create();
    private final Subject<LoginState> loginStateSubject = PublishSubject.create();

    @Before
    public void setUp() {
        userConnection = new UserConnection(connectionStateSubject, loginStateSubject);
        userObservable = userConnection
            .observe()
            .test();
    }

    @Test
    public void connectedIsMappedCorrect() {
        connectionStateSubject.onNext(ConnectionState.CONNECTED);

        userObservable.assertValue(UserConnectionState.CONNECTED);
    }

    @Test
    public void disconnectedIsMappedCorrect() {
        loginStateSubject.onNext(LoginState.LOGGED_IN);
        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

        userObservable.assertValue(UserConnectionState.DISCONNECTED);
    }

    @Test
    public void logoutIsMappedCorrect() {
        loginStateSubject.onNext(LoginState.LOGGED_OUT);
        connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

        userObservable.assertValue(UserConnectionState.LOGGED_OUT);
    }
}
