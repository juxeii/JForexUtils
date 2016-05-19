package com.jforex.programming.connection;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.jforex.programming.misc.JFObservable;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.system.IClient;

import rx.Completable;
import rx.Observable;

public class AuthentificationUtil {

    private final IClient client;
    private final JFObservable<LoginState> loginStatePublisher = new JFObservable<>();
    private final Observable<ConnectionState> connectionStateObs;
    private final StateMachineConfig<LoginState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<LoginState, FSMTrigger> fsm = new StateMachine<>(LoginState.LOGGED_OUT, fsmConfig);

    private enum FSMTrigger {
        CONNECTED,
        DISCONNECTED,
        LOGOUT
    }

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    public AuthentificationUtil(final IClient client,
                                final Observable<ConnectionState> connectionStateObs) {
        this.client = client;
        this.connectionStateObs = connectionStateObs;

        initConnectionStateObs(connectionStateObs);
        configureFSM();
    }

    private final void initConnectionStateObs(final Observable<ConnectionState> connectionStateObs) {
        connectionStateObs.subscribe(connectionState -> {
            if (connectionState == ConnectionState.CONNECTED)
                fsm.fire(FSMTrigger.CONNECTED);
            else
                fsm.fire(FSMTrigger.DISCONNECTED);
        });
    }

    private final void configureFSM() {
        fsmConfig.configure(LoginState.LOGGED_OUT)
                .onEntry(() -> loginStatePublisher.onNext(LoginState.LOGGED_OUT))
                .permit(FSMTrigger.CONNECTED, LoginState.LOGGED_IN)
                .ignore(FSMTrigger.DISCONNECTED)
                .ignore(FSMTrigger.LOGOUT);

        fsmConfig.configure(LoginState.LOGGED_IN)
                .onEntry(() -> loginStatePublisher.onNext(LoginState.LOGGED_IN))
                .permit(FSMTrigger.LOGOUT, LoginState.LOGGED_OUT)
                .ignore(FSMTrigger.CONNECTED)
                .ignore(FSMTrigger.DISCONNECTED);
    }

    public final Observable<LoginState> loginStateObs() {
        return loginStatePublisher.get();
    }

    public LoginState loginState() {
        return fsm.getState();
    }

    public Completable login(final LoginCredentials loginCredentials) {
        final Optional<Exception> exceptionOpt = connectClient(loginCredentials.jnlpAddress(),
                                                               loginCredentials.username(),
                                                               loginCredentials.password(),
                                                               loginCredentials.pinOpt());
        return exceptionOpt.isPresent()
                ? Completable.error(exceptionOpt.get())
                : Completable.create(subscriber -> {
                    connectionStateObs.take(1)
                            .subscribe(connectionState -> {
                                if (connectionState == ConnectionState.CONNECTED)
                                    subscriber.onCompleted();
                                else
                                    subscriber.onError(new ConnectException());
                            });
                }).timeout(platformSettings.logintimeoutseconds(), TimeUnit.SECONDS);
    }

    private final Optional<Exception> connectClient(final String jnlpAddress,
                                                    final String username,
                                                    final String password,
                                                    final Optional<String> pinOpt) {
        try {
            if (pinOpt.isPresent())
                client.connect(jnlpAddress, username, password, pinOpt.get());
            else
                client.connect(jnlpAddress, username, password);
        } catch (final Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    public final void logout() {
        if (isLoggedIn()) {
            client.disconnect();
            fsm.fire(FSMTrigger.LOGOUT);
        }
    }

    private boolean isLoggedIn() {
        return loginState() == LoginState.LOGGED_IN;
    }

    public void reconnect() {
        if (isLoggedIn())
            client.reconnect();
    }
}
