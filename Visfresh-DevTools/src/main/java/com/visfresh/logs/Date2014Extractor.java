/**
 *
 */
package com.visfresh.logs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Date2014Extractor extends AbstractVisfreshLogParser {
    private InputStream input;
    private OutputStream output;

    /**
     * @param in input stream.
     * @param out output stream.
     */
    public Date2014Extractor(final InputStream in, final OutputStream out) {
        super();
        this.input = in;
        this.output = out;
    }

    public void extraceRawData() throws IOException {
        parse(input);
    }
    /* (non-Javadoc)
     * @see com.visfresh.logs.AbstractVisfreshLogParser#handleNextLogUnit(com.visfresh.logs.LogUnit)
     */
    @Override
    protected void handleNextLogUnit(final LogUnit u) {
        try {
            if (acceptUnit(u)) {
                output.write(u.getRawData());
                output.flush();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param u log unit.
     * @return true if accept.
     */
    protected boolean acceptUnit(final LogUnit u) {
        return contains4214(u.getMessage()) && u.getLocation().contains("DeviceCommunicationServlet");
    }
    /**
     * @param message
     * @return
     */
    private boolean contains4214(final String message) {
        if (message.contains("|2014/")) {
            return true;
        }
        return false;
    }

    public static void main(final String[] args) throws IOException {
//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");
        final File outFile = new File(inFile.getParentFile(), "2014-" + inFile.getName());

        final InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        try {
            final OutputStream out = new FileOutputStream(outFile);
            try {
                final Date2014Extractor e = new Date2014Extractor(in, out);
                e.extraceRawData();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
