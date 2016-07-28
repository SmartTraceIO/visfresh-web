/**
 *
 */
package com.visfresh.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@RestController("Reports")
@RequestMapping("/rest")
public class ReportsController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);
    private static MediaType OCTET_STREAM = MediaType.parseMediaType("application/octet-stream");

    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private PerformanceReportDao performanceReportDao;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public ReportsController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/getPerformanceReport/{authToken}",
            method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<?> getPerformanceReport(
            @PathVariable final String authToken,
            @RequestParam(required = false) final String startDate,
            @RequestParam(required = false) final String endDate)
            throws Exception {
        final User user = getLoggedInUser(authToken);

        checkAccess(user, Role.BasicUser);

        Date d1 = null;
        Date d2 = null;

        if (startDate != null || endDate != null) {
            final DateFormat df = DateTimeUtils.createDateFormat(
                    "yyyy-MM-dd", user.getLanguage(), user.getTimeZone());
            //start date
            if (startDate != null) {
                d1 = df.parse(startDate);
            }
            //end date
            if (endDate != null) {
                d2 = df.parse(endDate);

                //correct end date to end of day
                final Calendar calendar = new GregorianCalendar();
                calendar.setTime(d2);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                d2 = calendar.getTime();
            }
        }

        //correct null date ranges
        if (d2 == null) {
            d2 = new Date();
        }
        if (d1 == null) {
            d1 = new Date(d2.getTime() - 30 * 24 * 60 * 60 * 1000l);
        }

        //create report bean.
        final PerformanceReportBean bean = performanceReportDao.createReport(
                user.getCompany(), d1, d2);

        //create tmp file with report PDF content.
        final File file = createTmpFile("performanceReport");

        try {
            final OutputStream out = new FileOutputStream(file);
            try {
                reportBuilder.createPerformanceReport(bean, user, out);
            } finally {
                out.close();
            }
        } catch (final Throwable e) {
            log.error("Failed to create pefromance report", e);
            file.delete();
            throw new IOException("Failed to create performance report", e);
        }

        final InputStream in = new TmpFileInputStream(file);
        return ResponseEntity
                .ok()
                .contentType(OCTET_STREAM)
                .contentLength(file.length())
                .body(new InputStreamResource(in));
    }
    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/getShipmentReport/{authToken}",
            method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<?> getShipmentReport(
            @PathVariable final String authToken,
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) throws Exception {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            log.error("Incorrect shipment request parameters. Shipment ID: " + shipmentId
                    + ", SN: " + sn + ", trip count: " + trip);
            throw new IOException("Should be specified shipmentId or (sn and trip) request parameters");
        }

        final User user = getLoggedInUser(authToken);

        checkAccess(user, Role.BasicUser);

        final Shipment s;
        if (shipmentId != null) {
            s = shipmentDao.findOne(shipmentId);
        } else {
            s = shipmentDao.findBySnTrip(user.getCompany(), sn, trip);
        }

        checkCompanyAccess(user, s);
        if (s == null) {
            log.error("Shipment not found. Shipment ID: " + shipmentId
                    + ", SN: " + sn + ", trip count: " + trip);
            throw new IOException("Shipment not found");
        }

        //create report bean.
        final ShipmentReportBean bean = createShipmentReport(s);

        //create tmp file with report PDF content.
        final File file = createTmpFile("performanceReport");

        try {
            final OutputStream out = new FileOutputStream(file);
            try {
                reportBuilder.createShipmentReport(bean, user, out);
            } finally {
                out.close();
            }
        } catch (final Throwable e) {
            log.error("Failed to create pefromance report", e);
            file.delete();
            throw new IOException("Failed to create performance report", e);
        }

        final InputStream in = new TmpFileInputStream(file);
        return ResponseEntity
                .ok()
                .contentType(OCTET_STREAM)
                .contentLength(file.length())
                .body(new InputStreamResource(in));
    }

    /**
     * @param shipment shipment.
     * @return shipment report bean.
     */
    private ShipmentReportBean createShipmentReport(final Shipment shipment) {
        final ShipmentReportBean bean = new ShipmentReportBean();
//      bean.set
        return bean;
    }

    /**
     * @param name resource name.
     * @return temporary file.
     * @throws IOException
     */
    private File createTmpFile(final String name) throws IOException {
        return File.createTempFile("visfreshtmp-", "-" + name);
    }
}
