/**
 *
 */
package com.visfresh.impl.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.entities.User;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PdfReportBuilderImpl implements PdfReportBuilder {
    private static final Logger log = LoggerFactory.getLogger(PdfReportBuilderImpl.class);
    private static final String APPLICATION_NAME = "reports-1.0.2-app";

    private WeakReference<PdfReportBuilder> ref = new WeakReference<PdfReportBuilder>(null);
    private int numUsed;

    /**
     * Default constructor.
     */
    public PdfReportBuilderImpl() {
        super();
    }

    @PostConstruct
    public void unzipReportApp() throws FileNotFoundException, IOException {
        final File workDir = getWorkDir();
        if (workDir.exists()) {
            log.debug("PDF report application files already installed");
            return;
        }

        log.debug("Unzip PDF report application to "  + workDir.getAbsolutePath());
        workDir.mkdirs();

        //unzip application to work dir.
        final ZipInputStream in = new ZipInputStream(PdfReportBuilderImpl.class.getClassLoader()
                .getResourceAsStream(APPLICATION_NAME + ".zip"));
        try {
            ZipEntry e;
            while((e = in.getNextEntry()) != null) {
                final OutputStream out = new FileOutputStream(new File(workDir, e.getName()));
                try {
                    final byte[] buff = new byte[512];
                    int len;
                    while ((len = in.read(buff, 0, buff.length)) > -1) {
                        out.write(buff, 0, len);
                        out.flush();
                    }
                } finally {
                    out.close();
                }
            }
        } finally {
            in.close();
        }
    }

    /**
     * @return
     */
    protected File getWorkDir() {
        return new File(System.getProperty("user.home")
                + File.separator + "smart-trace"
                + File.separator + "pdf-builder"
                + File.separator + APPLICATION_NAME);
    }

    /* (non-Javadoc)
     * @see com.visfresh.reports.PdfReportBuilder#createPerformanceReport(com.visfresh.reports.performance.PerformanceReportBean, com.visfresh.entities.User, java.io.OutputStream)
     */
    @Override
    public void createPerformanceReport(final PerformanceReportBean bean, final User user, final OutputStream out) throws IOException {
        getDelegate().createPerformanceReport(bean, user, out);
    }

    /* (non-Javadoc)
     * @see com.visfresh.reports.PdfReportBuilder#createShipmentReport(com.visfresh.reports.shipment.ShipmentReportBean, com.visfresh.entities.User, java.io.OutputStream)
     */
    @Override
    public void createShipmentReport(final ShipmentReportBean bean, final User user, final OutputStream out) throws IOException {
        getDelegate().createShipmentReport(bean, user, out);
    }

    private synchronized PdfReportBuilder getDelegate() {
        PdfReportBuilder d = ref.get();
        if (d == null || numUsed > 10) {
            log.debug("Load PDF report builder instance");
            numUsed = 0;
            d = loadDelegate();
            ref = new WeakReference<PdfReportBuilder>(d);
            log.debug("PDF report builder instance has loaded");
        } else {
            log.debug("Old PDF report builder instance is reused");
        }

        numUsed++;
        return d;
    }

    /**
     * @return
     * @throws IOException
     */
    private PdfReportBuilder loadDelegate() {
        try {
            final File[] jars = getWorkDir().listFiles();
            final URL[] urls = new URL[jars.length];
            for (int i = 0; i < jars.length; i++) {
                urls[i] = jars[i].toURI().toURL();
            }

            @SuppressWarnings("resource")
            final ClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
            return (PdfReportBuilder) loader.loadClass(
                    "com.visfresh.reports.JasperDrReportBuilder").newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
