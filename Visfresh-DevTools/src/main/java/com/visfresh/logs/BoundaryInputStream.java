package com.visfresh.logs;

import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BoundaryInputStream extends AbstractBoundaryInputStream {
    /**
     * The limit text.
     */
    private final byte[] limit;

    /**
     * @param originStream the origin input stream.
     * @param limitText the text marker of end of stream.
     */
    public BoundaryInputStream(final InputStream originStream, final String limitText) {
        super(originStream, limitText.length());
        if (limitText.length() == 0) {
            throw new IllegalArgumentException("Zero length of limit string");
        }
        limit = limitText.getBytes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean searchBounds() {
        return Arrays.equals(buffer, limit);
    }
}
