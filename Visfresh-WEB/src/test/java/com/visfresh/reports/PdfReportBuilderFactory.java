/**
 *
 */
package com.visfresh.reports;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.visfresh.impl.services.PdfReportBuilderImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class PdfReportBuilderFactory {
    /**
     * Default constructor.
     */
    private PdfReportBuilderFactory() {
        super();
    }

    public static PdfReportBuilder createReportBuilder() throws FileNotFoundException, IOException {
        final PdfReportBuilderImpl impl = new PdfReportBuilderImpl();
        impl.unzipReportApp();
        return impl;
    }
}
