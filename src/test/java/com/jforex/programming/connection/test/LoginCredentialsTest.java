package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.test.common.CommonUtilForTest;

public class LoginCredentialsTest extends CommonUtilForTest {

    @Test
    public void testPinIsSet() {
        assertThat(loginCredentialsWithPin.jnlpAddress(), equalTo(jnlpAddress));
        assertThat(loginCredentialsWithPin.username(), equalTo(username));
        assertThat(loginCredentialsWithPin.password(), equalTo(password));
        assertThat(loginCredentialsWithPin.maybePin().get(), equalTo(pin));
    }

    @Test
    public void testPinIsEmptyWhenNotSet() {
        assertThat(loginCredentials.jnlpAddress(), equalTo(jnlpAddress));
        assertThat(loginCredentials.username(), equalTo(username));
        assertThat(loginCredentials.password(), equalTo(password));
        assertFalse(loginCredentials.maybePin().isPresent());
    }
}
