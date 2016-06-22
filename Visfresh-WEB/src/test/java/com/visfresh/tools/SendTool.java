/**
 *
 */
package com.visfresh.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class SendTool extends JPanel {
    private static final long serialVersionUID = -6202773383101827526L;

    private final JTextField url = new JTextField("https://smarttrace.com.au/web/vf/rest/");
    private final JTextArea request = new JTextArea();
    private final JTextArea response = new JTextArea();

    /**
     * Default constructor.
     */
    private SendTool() {
        super(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        final int gap = 5;

        //Top panel (URL)
        final JPanel urlPanel = new JPanel(new BorderLayout(gap, gap));
        urlPanel.add(new JLabel("URL:"), BorderLayout.WEST);
        urlPanel.add(url, BorderLayout.CENTER);
        add(urlPanel, BorderLayout.NORTH);

        //center panel
        final JPanel center = new JPanel(new GridLayout(1, 2, 5, 5));
        center.setBorder(new CompoundBorder(
                new BevelBorder(BevelBorder.RAISED),
                new EmptyBorder(5, 5, 5, 5)));
        center.add(createTitledPane("Request", request));
        center.add(createTitledPane("Response", response));
        add(center, BorderLayout.CENTER);

        //south panel
        final JPanel buttons = new JPanel(new FlowLayout());
        add(buttons, BorderLayout.SOUTH);

        final JButton send = new JButton("Send");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doSend((JButton) e.getSource());
            }
        });
        buttons.add(send);
    }

    /**
     * @param title
     * @param textArea
     * @return
     */
    private JPanel createTitledPane(final String title, final JTextArea textArea) {
        textArea.setWrapStyleWord(true);
        final JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(textArea), BorderLayout.CENTER);
        p.setBorder(new CompoundBorder(new TitledBorder(new EmptyBorder(5, 0, 0, 0), title),
                new BevelBorder(BevelBorder.LOWERED)));
        return p;
    }

    /**
     *
     */
    protected void doSend(final JButton source) {
        source.setEnabled(false);
        response.setText("");

        final SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected String doInBackground() throws Exception {
                return sendRequestToService();
            }
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done() {
                source.setEnabled(true);
                try {
                    final String result = get();
                    response.setText(result);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * @return
     */
    protected String sendRequestToService() throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(this.url.getText()).openConnection();
        con.setDoInput(true);

        final String text = request.getText();
        if (text != null && text.length() > 0) {
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            final OutputStream out = con.getOutputStream();
            try {
                out.write(text.getBytes());
                out.flush();
            } finally {
                out.close();
            }
        } else {
            con.setDoOutput(false);
        }

        try {
            final InputStream in = con.getInputStream();
            return readInput(in);
        } catch (final Exception e) {
            return readInput(con.getErrorStream());
        }
    }

    /**
     * @param in input stream.
     * @return
     * @throws IOException
     */
    private String readInput(final InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try {
            final byte[] buff = new byte[256];
            int len;

            while ((len = in.read(buff)) > -1) {
                sb.append(new String(buff, 0, len));
            }
        } finally {
            in.close();
        }

        return sb.toString();
    }

    public static void main(final String[] args) {
        final JFrame f = new JFrame();
        f.setTitle("Send Anything to Anywhere");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setContentPane(new SendTool());
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = screen.width * 2 / 3;
        final int h = screen.height * 2 / 3;

        f.setBounds(
                (screen.width - w) / 2,
                (screen.height - h) / 2,
                w,
                h);
        f.setVisible(true);
    }
}
