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
public class RawDataExtractor extends AbstractVisfreshLogParser {
    private String device;
    private InputStream input;
    private OutputStream output;

    /**
     * @param in input stream.
     * @param out output stream.
     */
    public RawDataExtractor(final InputStream in, final OutputStream out) {
        super();
        this.input = in;
        this.output = out;
    }

    public void setDevice(final String imei) {
        this.device = imei;
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
        return containsDevice(u.getMessage()) && u.getLocation().contains("DeviceCommunicationServlet");
    }
    /**
     * @param message
     * @return
     */
    private boolean containsDevice(final String message) {
        return device != null && message.contains(device);
    }

    public static void main(final String[] args) throws IOException {
        final String device = "354430070008215";

//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log.1");
        final File outFile = new File(inFile.getParentFile(), device + "-" + inFile.getName());

        final InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        try {
            final OutputStream out = new FileOutputStream(outFile);
            try {
                final RawDataExtractor e = new RawDataExtractor(in, out);
                e.setDevice(device);
                e.extraceRawData();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
