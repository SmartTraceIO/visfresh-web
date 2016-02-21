package com.visfresh.logs;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractBoundaryInputStream extends InputStream {
    /**
     * The byte buffer.
     */
    protected final byte[] buffer;
    /**
     * Origin input stream.
     */
    protected final InputStream in;

    /**
     * @param originStream the origin input stream.
     * @param limitText the text marker of end of stream.
     */
    public AbstractBoundaryInputStream(final InputStream originStream, final int buffLength) {
        super();
        this.in = originStream;
        buffer = new byte[buffLength];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (searchBounds()) {
            return -1;
        }

        final int ch = in.read();
        if (ch > -1) {
            //Shift buffer.
            for (int i = 1; i < buffer.length; i++) {
                buffer[i - 1] = buffer[i];
            }
            buffer[buffer.length - 1] = (byte) ch;
        }

        return ch;
    }

    /**
     * @return true if is bound reached.
     */
    protected abstract boolean searchBounds();
}
