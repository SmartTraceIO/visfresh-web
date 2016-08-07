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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.entities.Device;
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
    public static final String GET_SHIPMENT_REPORT = "getShipmentReport";
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private PerformanceReportDao performanceReportDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private ShipmentReportDao shipmentReportDao;
    @Autowired
    private FileDownloadController fileDownload;

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
    @RequestMapping(value = "/getPerformanceReport/{authToken}", method = RequestMethod.GET)
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
        final File file = fileDownload.createTmpFile("performanceReport.pdf");

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
                .contentType(MediaType.parseMediaType("application/pdf"))
                .contentLength(file.length())
                .body(new InputStreamResource(in));
    }
    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/" + GET_SHIPMENT_REPORT + "/{authToken}", method = RequestMethod.GET)
    public void getShipmentReport(
            @PathVariable final String authToken,
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip,
            final HttpServletRequest request,
            final HttpServletResponse response
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
        final ShipmentReportBean bean = shipmentReportDao.createReport(s, user);

        //create tmp file with report PDF content.
        final File file = fileDownload.createTmpFile(createFileName(s));

        try {
            final OutputStream out = new FileOutputStream(file);
            try {
                reportBuilder.createShipmentReport(bean, user, out);
            } finally {
                out.close();
            }
        } catch (final Throwable e) {
            log.error("Failed to create shipment report for " + s.getId(), e);
            file.delete();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final int index = request.getRequestURL().indexOf("/" + GET_SHIPMENT_REPORT);
        response.sendRedirect(FileDownloadController.createDownloadUrl(request.getRequestURL().substring(0, index),
                authToken, file.getName()));
    }
    /**
     * @param device
     * @param startDate
     * @param endDate
     * @return
     */
    private String createFileName(final Shipment s) {
        //normalize device serial number
        final StringBuilder sb = new StringBuilder(Device.getSerialNumber(s.getDevice().getImei()));
        while (sb.charAt(0) == '0' && sb.length() > 1) {
            sb.deleteCharAt(0);
        }

        //add shipment trip count
        sb.append('(');
        sb.append(s.getTripCount());
        sb.append(')');
        sb.insert(0, "shipment-");
        sb.append(".pdf");
        return sb.toString();
    }
}
