package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.test.common.CommonUtilForTest;

public class LoginCredentialsTest extends CommonUtilForTest {

    private void assertCommonAttributes() {
        assertThat(loginCredentials.jnlpAddress(), equalTo(jnlpAddress));
        assertThat(loginCredentials.username(), equalTo(username));
        assertThat(loginCredentials.password(), equalTo(password));
    }

    @Test
    public void testPinIsSet() {
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                username,
                                                password,
                                                pin);

        assertCommonAttributes();
        assertThat(loginCredentials.maybePin().get(), equalTo(pin));
    }

    @Test
    public void testPinIsEmptyWhenNotSet() {
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                username,
                                                password);

        assertCommonAttributes();
        assertFalse(loginCredentials.maybePin().isPresent());
    }
}
