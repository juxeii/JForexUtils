package com.jforex.programming.client.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.client.PinCaptcha;
import com.jforex.programming.test.common.CommonUtilForTest;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class PinCaptchaTest extends CommonUtilForTest {

    private PinCaptcha pinCaptcha;

    private final BufferedImage bufferedImage = new BufferedImage(2, 2, 2);

    @Before
    public void setUp() throws Exception {
        pinCaptcha = new PinCaptcha(clientMock);
    }

    @Test
    public void forAWTCallsOnClient() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenReturn(bufferedImage);

        final Optional<BufferedImage> maybeImage = pinCaptcha.forAWT(jnlpAddress);

        assertThat(maybeImage.get(), equalTo(bufferedImage));
        verify(clientMock).getCaptchaImage(jnlpAddress);
    }

    @Test
    public void forAWTReturnsEmptyOptionalWhenClientThrows() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenThrow(jfException);

        final Optional<BufferedImage> maybeImage = pinCaptcha.forAWT(jnlpAddress);

        assertFalse(maybeImage.isPresent());
    }

    @Test
    public void pinCaptchaForJavaFXCallsOnClient() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenReturn(bufferedImage);

        final Optional<Image> maybeImage = pinCaptcha.forJavaFX(jnlpAddress);

        assertThat(maybeImage.get(), instanceOf(WritableImage.class));
        verify(clientMock).getCaptchaImage(jnlpAddress);
    }

    @Test
    public void pinCaptchaForJavaFXReturnsEmptyOptionalWhenClientThrows() throws Exception {
        when(clientMock.getCaptchaImage(jnlpAddress)).thenThrow(jfException);

        final Optional<Image> maybeImage = pinCaptcha.forJavaFX(jnlpAddress);

        assertFalse(maybeImage.isPresent());
    }
}
