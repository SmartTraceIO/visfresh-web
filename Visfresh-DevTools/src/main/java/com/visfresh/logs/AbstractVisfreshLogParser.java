package com.visfresh.logs;


import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractVisfreshLogParser {
    private static final String ENCODING = "UTF-8";

    /**
     * The constructor.
     */
    public AbstractVisfreshLogParser() {
        super();
    }

    /**
     * @param in the input stream.
     * @throws IOException I/O exception.
     */
    public void parse(final InputStream origin) throws IOException {
        LogParseInputStream in = new LogParseInputStream(origin);

        StringUtils.getContent(in, ENCODING);
        String logUnitStart = in.getBound();

        while (true) {
            in = new LogParseInputStream(origin);
            final String data = StringUtils.getContent(in, ENCODING);
            if (data == null || data.length() == 0) {
                break;
            }

            final String nextBound = in.getBound();
            final String body = nextBound == null ? data : data.substring(0, data.length() - nextBound.length());

            //read next block
            final LogUnit u = createLogUnitFromLogPrefix(logUnitStart);
            u.setMessage(body);
            //raw data
            u.setRawData(toBytes(logUnitStart, body));

            handleNextLogUnit(u);

            //read next bound
            logUnitStart = nextBound;
        }
    }

    /**
     * @param logUnitStart
     * @param body
     * @return
     */
    private byte[] toBytes(final String logUnitStart, final String body) {
        final byte[] bytes = new byte[logUnitStart.length() + body.length()];
        System.arraycopy(logUnitStart.getBytes(), 0, bytes, 0, logUnitStart.length());
        System.arraycopy(body.getBytes(), 0, bytes, logUnitStart.length(), body.length());
        return bytes;
    }

    /**
     * @param u the log unit.
     */
    protected abstract void handleNextLogUnit(final LogUnit u);

    /**
     * @param prefix the log start string.
     * @return the logging unit.
     * @throws IOException I/O exception
     */
    private LogUnit createLogUnitFromLogPrefix(final String prefix) throws IOException {
        final String[] split = prefix.split("  *");

        final LogUnit u = new LogUnit();
        //read date.
        u.setDate(parseDate(split[0] + " " + split[1]));
        //read log level
        u.setLevel(split[2]);
        //read location
        u.setLocation(split[3].substring(1, split[3].length() - 1));
        return u;
    }

    /**
     * @param str the string date representation.
     * @return the date.
     */
    private Date parseDate(final String str) {
        final DateFormat fmt = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss,SSS");
        try {
            return fmt.parse(str);
        } catch (final ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
