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
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RawDataExtractor extends AbstractVisfreshLogParser {
    private Set<String> devices = new HashSet<String>();
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

    public void addDevice(final String imei) {
        devices.add(imei);
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
            if (containsDevice(u.getMessage())) {
                output.write(u.getRawData());
                output.flush();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param message
     * @return
     */
    private boolean containsDevice(final String message) {
        for (final String imei : devices) {
            if (message.contains(imei)) {
                return true;
            }
        }
        return false;
    }

    public static void main(final String[] args) throws IOException {
        final String device = "354430070007613";

        final File inFile = new File("/home/soldatov/tmp/visfresh-dcs.log");
        final File outFile = new File(inFile.getParentFile(), device + ".log");

        final InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        try {
            final OutputStream out = new FileOutputStream(outFile);
            try {
                final RawDataExtractor e = new RawDataExtractor(in, out);
                e.addDevice(device);
                e.extraceRawData();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
