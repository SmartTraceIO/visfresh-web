/**
 *
 */
package com.visfresh.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class DeviceEmulator extends JFrame {
    private static final long serialVersionUID = -7071929763282414997L;
    private final JTextArea request = new JTextArea();
    private final JTextField url = new JTextField();

    /**
     * @throws HeadlessException
     */
    private DeviceEmulator() throws HeadlessException {
        super("Device emulator");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        url.setText("http://localhost:8080/data");
        request.setText("358688000000158|AUT|2013/10/18 13:28:29|\n"
                + "4023|-10.24|\n"
                + "460|1|9533|16114|34|\n"
                + "460|1|9533|16111|37|\n"
                + "460|1|9533|16904|31|\n"
                + "460|1|9533|16113|23|\n"
                + "460|1|9533|16142|21|\n"
                + "460|1|9533|16526|18|");

        final JPanel cp = new JPanel(new BorderLayout(10, 10));
        setContentPane(cp);
        cp.setBorder(new  EmptyBorder(5, 5, 5, 5));

        cp.add(url, BorderLayout.NORTH);
        cp.add(new JScrollPane(request), BorderLayout.CENTER);

        final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cp.add(south, BorderLayout.SOUTH);

        final JButton send = new JButton("Send");
        south.add(send);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    sendDeviceRequest();
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    /**
     *
     */
    protected void sendDeviceRequest() throws IOException {
        final URI uri = URI.create(url.getText());
        final int port = uri.getPort() == -1 ? 80 : uri.getPort();
        final byte[] body = request.getText().getBytes();

        final Socket so = new Socket(uri.getHost(), port);
        try {
            final OutputStream out = so.getOutputStream();
            //POST /data HTTP/1.1<CR><LF> HTTP request
            out.write(("POST " + uri.getPath() + " HTTP/1.1\r\n").getBytes());
            //Host: 183.13.213.153<CR><LF> HTTP header line
            out.write(("Host: " + uri.getHost() + "\r\n").getBytes());
            //Accept-Encoding: identity<CR><LF> HTTP header line
            out.write(("Accept-Encoding: identity\r\n").getBytes());
            //Content-Length: 578<CR><LF> HTTP header line
            out.write(("Content-Length: " + body.length + "\r\n\r\n").getBytes());

            //<CR><LF> Blank line - gap between header and body
            out.write(body);
            out.flush();

            //read input
            print(so.getInputStream());
        } finally {
            so.close();
        }
    }

    /**
     * @param in
     */
    private void print(final InputStream in) throws IOException {
        int len;
        final byte[] buff = new byte[128];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((len = in.read(buff)) > -1) {
            out.write(buff, 0, len);
        }

        System.out.println(new String(out.toByteArray()));
    }

    public static void main(final String[] args) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        final DeviceEmulator em = new DeviceEmulator();
        em.setSize(screen.width * 2 / 3, screen.height * 2 / 3);
        em.setLocation((screen.width - em.getWidth()) / 2, (screen.height - em.getHeight()) / 2);

        em.setVisible(true);
    }
}
