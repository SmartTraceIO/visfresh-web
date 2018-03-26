/**
 *
 */
package com.visfresh;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class MessageParserUtils {
    /**
     * Default constructor.
     */
    private MessageParserUtils() {
        super();
    }

    /**
     * @param reader reader.
     * @return stream content as string.
     * @throws IOException
     */
    public static String getContent(final Reader reader) throws IOException {
        final StringWriter sw = new StringWriter();

        int len;
        final char[] buff = new char[128];
        while ((len = reader.read(buff)) > -1) {
            sw.write(buff, 0, len);
        }

        return sw.toString();
    }
}
