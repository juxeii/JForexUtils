package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.connection.LoginCredentials;

public class LoginCredentialsTest {

    private LoginCredentials loginCredentials;

    private final String jnlpAddres = "http://test";
    private final String userName = "john";
    private final String password = "doe";

    @Test
    public void testPinIsSet() {
        final String pin = "1234";
        loginCredentials = new LoginCredentials(jnlpAddres,
                                                userName,
                                                password,
                                                pin);

        assertThat(loginCredentials.maybePin().get(), equalTo(pin));
    }

    @Test
    public void testPinIsEmptyWhenNotSet() {
        loginCredentials = new LoginCredentials(jnlpAddres,
                                                userName,
                                                password);

        assertFalse(loginCredentials.maybePin().isPresent());
    }
}
