package com.visfresh.logs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LogParseInputStreamTest {
    /**
     * The constructor.
     */
    public LogParseInputStreamTest() {
        super();
    }

    /**
     * @throws IOException I/O exception.
     */
    @Test
    public void testParse() throws IOException {
        final String text = "al;kj=p[]]qon;aasd \n:::";
        final String limit = "2016-02-17 06:27:41,276 WARN  [DeviceCommunicationServlet_abrakadabralkj]";
        final String suffix = "qopwtiu []{}:;;jon gh";

        final InputStream in = new ByteArrayInputStream((text + limit + suffix).getBytes());
        assertEquals(text + limit, StringUtils.getContent(new LogParseInputStream(in), "UTF-8"));
        assertEquals(suffix, StringUtils.getContent(in, "UTF-8"));
    }
}
