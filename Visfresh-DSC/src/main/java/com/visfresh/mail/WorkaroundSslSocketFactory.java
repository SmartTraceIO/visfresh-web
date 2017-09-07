/**
 *
 */
package com.visfresh.mail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WorkaroundSslSocketFactory extends MailSSLSocketFactory {
    private final SSLSocketFactory factory;

    /**
     * @throws GeneralSecurityException
     */
    public WorkaroundSslSocketFactory() throws GeneralSecurityException {
        super();
        factory = getDefaultFactory();
    }
    /**
     * @param protocol
     * @throws GeneralSecurityException
     */
    public WorkaroundSslSocketFactory(final String protocol)
            throws GeneralSecurityException {
        super(protocol);
        factory = getDefaultFactory();
    }
    /**
     * @throws GeneralSecurityException
     *
     */
    protected SSLSocketFactory getDefaultFactory() throws GeneralSecurityException {
        try {
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        } catch (final Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    /**
     * @param so
     */
    protected SSLSocket updateSocket(final Socket so) {
        final SSLSocket socket = (SSLSocket) so;
        final String[] enabledCipherSuites = socket.getEnabledCipherSuites();

        // avoid hardcoding a new list, we just remove the entries
        // which cause the exception
        final List<String> asList = new ArrayList<String>(Arrays.asList(enabledCipherSuites));

        // we identified the following entries causeing the problems
        // "Could not generate DH keypair"
        // and
        // "Caused by: java.security.InvalidAlgorithmParameterException: Prime size must be multiple of 64, and can only range from 512 to 1024 (inclusive)"
        asList.remove("TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
        asList.remove("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA");
        asList.remove("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");

        final String[] array = asList.toArray(new String[0]);
        socket.setEnabledCipherSuites(array);

        return socket;
    }
    public static SSLSocketFactory getDefault() {
        try {
            return new WorkaroundSslSocketFactory();
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Creates an unconnected socket.
     *
     * @return the unconnected socket
     * @throws IOException
     * @see java.net.Socket#connect(java.net.SocketAddress, int)
     */
    @Override
    public Socket createSocket() throws IOException {
        return updateSocket(factory.createSocket());
    }

    /**
     * Constructs an SSL connection to a named host at a specified port.
     * This acts as the SSL client, and may authenticate itself or rejoin
     * existing SSL sessions allowed by the authentication context which
     * has been configured.
     *
     * @param host name of the host with which to connect
     * @param port number of the server's port
     */
    @Override
    public Socket createSocket(final String host, final int port)
    throws IOException, UnknownHostException
    {
        return updateSocket(factory.createSocket(host, port));
    }

    /**
     * Returns a socket layered over an existing socket to a
     * ServerSocket on the named host, at the given port.  This
     * constructor can be used when tunneling SSL through a proxy. The
     * host and port refer to the logical destination server.  This
     * socket is configured using the socket options established for
     * this factory.
     *
     * @param s the existing socket
     * @param host the server host
     * @param port the server port
     * @param autoClose close the underlying socket when this socket is closed
     *
     * @exception IOException if the connection can't be established
     * @exception UnknownHostException if the host is not known
     */
    @Override
    public Socket createSocket(final Socket s, final String host, final int port,
            final boolean autoClose) throws IOException {
        return updateSocket(factory.createSocket(s, host, port, autoClose));
    }


    /**
     * Constructs an SSL connection to a server at a specified address
     * and TCP port.  This acts as the SSL client, and may authenticate
     * itself or rejoin existing SSL sessions allowed by the authentication
     * context which has been configured.
     *
     * @param address the server's host
     * @param port its port
     */
    @Override
    public Socket createSocket(final InetAddress address, final int port)
    throws IOException
    {
        return updateSocket(factory.createSocket(address, port));
    }


    /**
     * Constructs an SSL connection to a named host at a specified port.
     * This acts as the SSL client, and may authenticate itself or rejoin
     * existing SSL sessions allowed by the authentication context which
     * has been configured. The socket will also bind() to the local
     * address and port supplied.
     */
    @Override
    public Socket createSocket(final String host, final int port,
        final InetAddress clientAddress, final int clientPort)
    throws IOException
    {
        return updateSocket(factory.createSocket(host, port, clientAddress, clientPort));
    }

    /**
     * Constructs an SSL connection to a server at a specified address
     * and TCP port.  This acts as the SSL client, and may authenticate
     * itself or rejoin existing SSL sessions allowed by the authentication
     * context which has been configured. The socket will also bind() to
     * the local address and port supplied.
     */
    @Override
    public Socket createSocket(final InetAddress address, final int port,
        final InetAddress clientAddress, final int clientPort)
    throws IOException
    {
        return updateSocket(factory.createSocket(address, port, clientAddress, clientPort));
    }


    /**
     * Returns the subset of the supported cipher suites which are
     * enabled by default.  These cipher suites all provide a minimum
     * quality of service whereby the server authenticates itself
     * (preventing person-in-the-middle attacks) and where traffic
     * is encrypted to provide confidentiality.
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    /**
     * Returns the names of the cipher suites which could be enabled for use
     * on an SSL connection.  Normally, only a subset of these will actually
     * be enabled by default, since this list may include cipher suites which
     * do not support the mutual authentication of servers and clients, or
     * which do not protect data confidentiality.  Servers may also need
     * certain kinds of certificates to use certain cipher suites.
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }
}
