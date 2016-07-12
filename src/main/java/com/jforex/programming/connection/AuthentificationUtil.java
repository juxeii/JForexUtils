package com.jforex.programming.connection;

import java.util.Optional;

import com.dukascopy.api.system.IClient;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.jforex.programming.misc.JFHotSubject;
import com.jforex.programming.misc.StreamUtil;

import rx.Completable;
import rx.Observable;

public class AuthentificationUtil {

    private final IClient client;
    private final JFHotSubject<LoginState> loginStateSubject = new JFHotSubject<>();
    private final StateMachineConfig<LoginState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<LoginState, FSMTrigger> fsm =
            new StateMachine<>(LoginState.LOGGED_OUT, fsmConfig);

    public enum FSMTrigger {
        CONNECTED,
        DISCONNECTED,
        LOGOUT
    }

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;

        initConnectionStateObs(connectionStateObs);
        configureFSM();
    }

    private final void
            initConnectionStateObs(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(connectionState -> {
            if (connectionState == ConnectionState.CONNECTED)
                fsm.fire(FSMTrigger.CONNECTED);
            else
                fsm.fire(FSMTrigger.DISCONNECTED);
        });
    }

    private final void configureFSM() {
        fsmConfig.configure(LoginState.LOGGED_OUT)
                .onEntry(() -> loginStateSubject.onNext(LoginState.LOGGED_OUT))
                .permit(FSMTrigger.CONNECTED, LoginState.LOGGED_IN)
                .ignore(FSMTrigger.DISCONNECTED)
                .ignore(FSMTrigger.LOGOUT);

        fsmConfig.configure(LoginState.LOGGED_IN)
                .onEntry(() -> loginStateSubject.onNext(LoginState.LOGGED_IN))
                .permit(FSMTrigger.LOGOUT, LoginState.LOGGED_OUT)
                .ignore(FSMTrigger.CONNECTED)
                .ignore(FSMTrigger.DISCONNECTED);
    }

    public final Observable<LoginState> loginStateObs() {
        return loginStateSubject.observable();
    }

    public LoginState loginState() {
        return fsm.getState();
    }

    public Completable loginCompletable(final LoginCredentials loginCredentials) {
        final String jnlpAddress = loginCredentials.jnlpAddress();
        final String username = loginCredentials.username();
        final String password = loginCredentials.password();
        final Optional<String> maybePin = loginCredentials.maybePin();

        return maybePin.isPresent()
                ? StreamUtil.CompletableFromJFRunnable(() -> client.connect(jnlpAddress,
                                                                            username,
                                                                            password,
                                                                            maybePin.get()))
                : StreamUtil.CompletableFromJFRunnable(() -> client.connect(jnlpAddress,
                                                                            username,
                                                                            password));
    }

    public final void logout() {
        if (isLoggedIn())
            client.disconnect();
        fsm.fire(FSMTrigger.LOGOUT);
    }

    private boolean isLoggedIn() {
        return loginState() == LoginState.LOGGED_IN;
    }
}
