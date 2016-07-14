package com.jforex.programming.connection.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.test.common.CommonUtilForTest;

public class LoginCredentialsTest extends CommonUtilForTest {

    @Test
    public void testPinIsSet() {
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                userName,
                                                password,
                                                pin);

        assertThat(loginCredentials.maybePin().get(), equalTo(pin));
    }

    @Test
    public void testPinIsEmptyWhenNotSet() {
        loginCredentials = new LoginCredentials(jnlpAddress,
                                                userName,
                                                password);

        assertFalse(loginCredentials.maybePin().isPresent());
    }
}
