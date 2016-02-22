package com.visfresh.logs;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LimitInputStream extends InputStream {
    /**
     *
     */
    private final long limit;
    /**
     * Origin input stream.
     */
    private final InputStream in;
    /**
     * The total read bytes.
     */
    private int total;

    /**
     * @param originStream the origin input stream.
     * @param limitText the text marker of end of stream.
     */
    public LimitInputStream(final InputStream originStream, final long limitText) {
        super();
        this.in = originStream;
        limit = limitText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (total == limit) {
            return -1;
        }

        final int ch = in.read();
        if (ch > -1) {
            total++;
        }

        return ch;
    }
}
