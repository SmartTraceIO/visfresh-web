/**
 *
 */
package com.visfresh.logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MultiDeviceRawDataExtractor extends AbstractVisfreshLogParser {
    private final Set<String> devices = new HashSet<>();
    private final File inFile;
    private InputStream input;
    private Map<String, OutputStream> outputs = new HashMap<>();

    /**
     * @param in input stream.
     * @param out output stream.
     * @throws IOException
     */
    public MultiDeviceRawDataExtractor(final File inFile) throws IOException {
        super();
        this.inFile = inFile;
        this.input = new FileInputStream(inFile);
    }

    public void addDevice(final String device) {
        devices.add(device);
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
                final String device = getDeviceFor(u.getMessage());
                if (device != null) {
                    final OutputStream out = getOrOpenOutputFor(device);
                    out.write(u.getRawData());
                    out.flush();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param device device.
     * @return
     * @throws FileNotFoundException
     */
    private OutputStream getOrOpenOutputFor(final String device) throws FileNotFoundException {
        OutputStream out = outputs.get(device);
        if (out == null) {
            final File outFile = new File(inFile.getParentFile(), device + "-" + inFile.getName());
            out = new FileOutputStream(outFile);
            outputs.put(device, out);
        }

        return out;
    }

    /**
     * @param u log unit.
     * @return true if accept.
     */
    protected boolean acceptUnit(final LogUnit u) {
        return u.getLocation().contains("DeviceCommunicationServlet");
    }
    private String getDeviceFor(final String message) {
        for (final String device : devices) {
            if (message.contains(device)) {
                return device;
            }
        }

        return null;
    }
    public void close() {
        try {
            input.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        for (final OutputStream out : outputs.values()) {
            try {
                out.flush();
                out.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        input = null;
        outputs.clear();
    }

    public static void main(final String[] args) throws IOException {
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");

        final MultiDeviceRawDataExtractor e = new MultiDeviceRawDataExtractor(inFile);
        e.addDevice("354430070000055");
        e.addDevice("354430070000162");
        e.addDevice("354430070003315");
        e.addDevice("354430070004164");
        e.addDevice("354430070004636");
        e.addDevice("354430070004792");
        e.addDevice("354430070004958");
        e.addDevice("354430070005013");
        e.addDevice("354430070005310");

        try {
            e.extraceRawData();
        } finally {
            e.close();
        }
    }
}
