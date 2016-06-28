/**
 *
 */
package com.visfresh.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.performance.AlertProfileStats;
import com.visfresh.reports.performance.BiggestTemperatureException;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.TemperatureRuleStats;
import com.visfresh.reports.shipment.ShipmentReportBean;

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
    public ResponseEntity<InputStream> getPerformanceReport(@PathVariable final String authToken)
            throws Exception {
        final User user = getLoggedInUser(authToken);

        checkAccess(user, Role.BasicUser);

        //create report bean.
        final PerformanceReportBean bean = createPerformanceReport(user.getCompany());

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
                .body(in);
    }
    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/getShipmentReport/{authToken}",
            method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<InputStream> getShipmentReport(@PathVariable final String authToken)
            throws Exception {
        final User user = getLoggedInUser(authToken);

        checkAccess(user, Role.BasicUser);

        //create report bean.
        final ShipmentReportBean bean = createShipmentReport(user.getCompany());

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
                .body(in);
    }

    /**
     * @param company
     * @return
     */
    private ShipmentReportBean createShipmentReport(final Company company) {
        return createShipmentReportBean();
    }
    /**
     * @param company
     * @return
     */
    private PerformanceReportBean createPerformanceReport(final Company company) {
        return createPerformanceBean();
    }

    /**
     * @return
     */
    private ShipmentReportBean createShipmentReportBean() {
        final ShipmentReportBean bean = new ShipmentReportBean();
        return bean;
    }
    /**
     * @return the bean to visualize.
     */
    private static PerformanceReportBean createPerformanceBean() {
        final PerformanceReportBean bean = new PerformanceReportBean();

        bean.setStartDate(new Date(System.currentTimeMillis() - 10000000000l));
        bean.setEndDate(new Date());
        bean.setNumberOfShipments(212);
        bean.setNumberOfTrackers(122);
        bean.setAvgShipmentsPerTracker(2.2);
        bean.setAvgTrackersPerShipment(1.4);

        bean.getAlertProfiles().add(createAlertProfile("Chilled Beef"));
        bean.getAlertProfiles().add(createAlertProfile("Chilled Wine"));
        return bean;
    }

    /**
     * @param name alert profile name.
     * @return random generated alert profile stats.
     */
    private static AlertProfileStats createAlertProfile(final String name) {
        final AlertType[] types = {AlertType.Hot, AlertType.CriticalHot, AlertType.Cold, AlertType.CriticalCold};
        final String[] serialNums = {"123", "324", "673", "257"};

        final Random random = new Random();
        final long oneHour = 60 * 60 * 1000l;

        final AlertProfileStats ap = new AlertProfileStats();
        ap.setAvgTemperature((random.nextDouble() - 0.5) * 20.);
        ap.setName(name);
        ap.setStandardDeviation(0.001 + random.nextDouble() / 0.5);
        ap.setTotalMonitoringTime((1 + random.nextInt(3 * 30 * 24)) * oneHour);

        final int numRules = 3 + random.nextInt(7);
        for (int i = 0; i < numRules; i++) {
            final TemperatureRuleStats rule = new TemperatureRuleStats();
            rule.setTotalTime((3 + random.nextInt(15)) * oneHour);

            //create temperature rule
            final TemperatureRule tr = new TemperatureRule();
            tr.setType(types[random.nextInt(types.length)]);
            tr.setTemperature((random.nextDouble() - 0.5) * 20.);
            tr.setCumulativeFlag(random.nextBoolean());
            tr.setTimeOutMinutes((3 + random.nextInt(15)) * 60);
            rule.setRule(tr);

            //add biggest exceptions
            final int numBidgest = random.nextInt(5);
            for (int j = 0; j < numBidgest; j++) {
                final BiggestTemperatureException b = new BiggestTemperatureException();
                b.setSerialNumber(serialNums[random.nextInt(serialNums.length)]);
                b.setTripCount(1 + random.nextInt(4));
                b.setTime((3 + random.nextInt(5)) * oneHour);
                rule.getBiggestExceptions().add(b);
            }

            ap.getTemperatureRules().add(rule);
        }

        return ap;
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
