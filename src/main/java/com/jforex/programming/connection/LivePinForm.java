package com.jforex.programming.connection;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

// Code from http://www.dukascopy.com/wiki/#JForex_SDK_LIVE_mode
public class LivePinForm {

    private final IClient client;
    private final String jnlpUrl;
    private static JFrame noParentFrame;

    private final static Logger logger = LogManager.getLogger(LivePinForm.class);

    public LivePinForm(final IClient client,
                       final String jnlpUrl) {
        this.client = client;
        this.jnlpUrl = jnlpUrl;
    }

    public String getPin() {
        PinDialog pd = null;
        try {
            pd = new PinDialog();
        } catch (final Exception e) {
            logger.error("getPin exc: " + e.getMessage());
        }
        return pd.pinfield.getText();
    }

    @SuppressWarnings("serial")
    private class PinDialog extends JDialog {

        private final JTextField pinfield = new JTextField();

        public PinDialog() throws Exception {
            super(noParentFrame, "PIN Dialog", true);

            final JPanel captchaPanel = new JPanel();
            captchaPanel.setLayout(new BoxLayout(captchaPanel, BoxLayout.Y_AXIS));

            final JLabel captchaImage = new JLabel();
            captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(jnlpUrl)));
            captchaPanel.add(captchaImage);

            captchaPanel.add(pinfield);
            getContentPane().add(captchaPanel);

            final JPanel buttonPane = new JPanel();

            final JButton btnLogin = new JButton("Login");
            buttonPane.add(btnLogin);
            btnLogin.addActionListener(e -> {
                setVisible(false);
                dispose();
            });

            final JButton btnReload = new JButton("Reload");
            buttonPane.add(btnReload);
            btnReload.addActionListener(e -> {
                try {
                    captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(jnlpUrl)));
                } catch (final Exception ex) {
                    logger.error("getPin exc: " + ex.getMessage());
                }
            });
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            setVisible(true);
        }
    }
}