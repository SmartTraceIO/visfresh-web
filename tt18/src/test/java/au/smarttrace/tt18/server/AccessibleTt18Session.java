/**
 *
 */
package au.smarttrace.tt18.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import au.smarttrace.tt18.IncorrectPacketLengthException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessibleTt18Session extends Tt18Session {
    /**
     * Default constructor.
     */
    public AccessibleTt18Session() {
        super(null, null);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.tt18.server.Tt18Session#processConnection(java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public void processConnection(final InputStream in, final OutputStream out)
            throws IOException, IncorrectPacketLengthException {
        super.processConnection(in, out);
    }
}
