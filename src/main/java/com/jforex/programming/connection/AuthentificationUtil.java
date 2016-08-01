package com.jforex.programming.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.system.IClient;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.jforex.programming.misc.JFHotSubject;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.misc.StreamUtil;

import rx.Completable;
import rx.Observable;

public class AuthentificationUtil {

    private final IClient client;
    private final JFHotSubject<LoginState> loginStateSubject = new JFHotSubject<>();
    private final StateMachineConfig<LoginState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<LoginState, FSMTrigger> fsm = new StateMachine<>(LoginState.LOGGED_OUT, fsmConfig);

    public enum FSMTrigger {

        CONNECT,
        DISCONNECT,
        LOGOUT
    }

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;

        initConnectionStateObs(connectionStateObs);
        configureFSM();
    }

    private final void initConnectionStateObs(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(connectionState -> {
            if (connectionState == ConnectionState.CONNECTED)
                fsm.fire(FSMTrigger.CONNECT);
            else
                fsm.fire(FSMTrigger.DISCONNECT);
        });
    }

    private final void configureFSM() {
        fsmConfig
                .configure(LoginState.LOGGED_OUT)
                .onEntry(() -> loginStateSubject.onNext(LoginState.LOGGED_OUT))
                .permit(FSMTrigger.CONNECT, LoginState.LOGGED_IN)
                .ignore(FSMTrigger.DISCONNECT)
                .ignore(FSMTrigger.LOGOUT);

        fsmConfig
                .configure(LoginState.LOGGED_IN)
                .onEntry(() -> loginStateSubject.onNext(LoginState.LOGGED_IN))
                .permit(FSMTrigger.LOGOUT, LoginState.LOGGED_OUT)
                .ignore(FSMTrigger.CONNECT)
                .ignore(FSMTrigger.DISCONNECT);
    }

    public final Observable<LoginState> loginStateObservable() {
        return loginStateSubject.observable();
    }

    public LoginState loginState() {
        return fsm.getState();
    }

    public Completable loginCompletable(final LoginCredentials loginCredentials) {
        final JFRunnable connectRunnable = checkNotNull(loginCredentials).maybePin().isPresent()
                ? loginRunnableWithPin(loginCredentials)
                : loginRunnableNoPin(loginCredentials);

        return StreamUtil.CompletableFromJFRunnable(connectRunnable);
    }

    private JFRunnable loginRunnableWithPin(final LoginCredentials loginCredentials) {
        return () -> client.connect(loginCredentials.jnlpAddress(),
                                    loginCredentials.username(),
                                    loginCredentials.password(),
                                    loginCredentials.maybePin().get());
    }

    private JFRunnable loginRunnableNoPin(final LoginCredentials loginCredentials) {
        return () -> client.connect(loginCredentials.jnlpAddress(),
                                    loginCredentials.username(),
                                    loginCredentials.password());
    }

    public final void logout() {
        if (loginState() == LoginState.LOGGED_IN)
            client.disconnect();
        fsm.fire(FSMTrigger.LOGOUT);
    }
}
