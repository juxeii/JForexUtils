package com.jforex.programming.connection;

import java.util.Optional;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public final class Authentification {

    private final IClient client;

    public Authentification(final IClient client) {
        this.client = client;
    }

    public final LoginResult login(final String jnlp,
                                   final String username,
                                   final String password) {
        return login(jnlp, username, password, "");
    }

    public final LoginResult loginWithPin(final String jnlp,
                                          final String username,
                                          final String password,
                                          final String pin) {
        return login(jnlp, username, password, pin);
    }

    private final LoginResult login(final String jnlp,
                                    final String username,
                                    final String password,
                                    final String pin) {
        try {
            if (pin.isEmpty())
                client.connect(jnlp, username, password);
            else
                client.connect(jnlp, username, password, pin);
            return new LoginResult(LoginResultType.LOGGED_IN, Optional.empty());
        } catch (final JFAuthenticationException e) {
            return new LoginResult(LoginResultType.INVALID_CREDENTIALS, Optional.of(e));
        } catch (final JFVersionException e) {
            return new LoginResult(LoginResultType.INVALID_VERSION, Optional.of(e));
        } catch (final Exception e) {
            return new LoginResult(LoginResultType.EXCEPTION, Optional.of(e));
        }
    }

    public final void logout() {
        client.disconnect();
    }
}
