/**
 *
 */
package com.visfresh.controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.EmailShipmentReportRequest;
import com.visfresh.io.json.ReportsSerializer;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.geomap.MapRendererImpl;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.services.EmailService;
import com.visfresh.services.EventsOptimizer;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@RestController("Reports")
@RequestMapping("/rest")
public class ReportsController extends AbstractController {
    public static final String GET_PERFORMANCE_REPORT = "getPerformanceReport";
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserDao userDao;

    //TODO remove after test
    @Autowired
    private TrackerEventDao trackerEventDao;
    //TODO remove after test
    @Autowired
    private EventsOptimizer eventsOptimizer;

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
    @RequestMapping(value = "/" + GET_PERFORMANCE_REPORT + "/{authToken}", method = RequestMethod.GET)
    public void getPerformanceReport(
            @PathVariable final String authToken,
            @RequestParam(required = false, value = "month") final String d,
            final HttpServletRequest request,
            final HttpServletResponse response
            )
            throws Exception {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            //calculate requested date in user's time zone.
            Date usersDate;
            if (d != null) {
                usersDate = new SimpleDateFormat("yyyy-MM").parse(d);
            } else {
                usersDate = DateTimeUtils.convertToTimeZone(new Date(), user.getTimeZone());
            }

            final Calendar c = new GregorianCalendar();
            c.setTime(usersDate);

            //calculate date ranges in user's time zone.
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);

            final Date endDate = c.getTime();

            //start date
            c.add(Calendar.MONTH, -2);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 1);

            final Date startDate = c.getTime();

            //create report bean.
            final PerformanceReportBean bean = performanceReportDao.createReport(
                    user.getCompany(), startDate, endDate);

            //create report bean.
            final File file = createPerformanceReport(bean, user);

            final int index = request.getRequestURL().indexOf("/" + GET_PERFORMANCE_REPORT);
            response.sendRedirect(FileDownloadController.createDownloadUrl(request.getRequestURL().substring(0, index),
                    authToken, file.getName()));

        } catch (final Exception e) {
            log.error("Failed to create performance report", e);
            throw e;
        }
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

        try {
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
            final File file = createShipmentReport(s, user);

            final int index = request.getRequestURL().indexOf("/" + GET_SHIPMENT_REPORT);
            response.sendRedirect(FileDownloadController.createDownloadUrl(request.getRequestURL().substring(0, index),
                    authToken, file.getName()));

        } catch (final Exception e) {
            log.error("Failed to create shipment report", e);
            throw e;
        }
    }
    /**
     * @param s shipment.
     * @param user user.
     * @return PDF shipment report.
     */
    private File createShipmentReport(final Shipment s, final User user) throws IOException {
        final ShipmentReportBean bean = shipmentReportDao.createReport(s);

//        final DateFormat fmt = DateTimeUtils.createDateFormat("yyyy-MM-dd_HH-mm",
//                user.getLanguage(), user.getTimeZone());
//
//        final String fileName = s.getCompany().getName().replaceAll("\\W", "_")
//                + " Shipment "
//                + s.getDevice().getSn()
//                + "("
//                + s.getTripCount()
//                + ")"
//                + " as of "
//                + fmt.format(new Date())
//                + DateTimeUtils.getTimeZoneString(user.getTimeZone().getRawOffset())
//                + ".pdf";
        final File file = createTmpFile(s, "pdf");

        final OutputStream out = new FileOutputStream(file);
        try {
            reportBuilder.createShipmentReport(bean, user, out);
        } finally {
            out.close();
        }
        return file;
    }
    /**
     * @param bean
     * @return
     */
    private File createPerformanceReport(final PerformanceReportBean bean, final User user) throws IOException {
        String companyName = bean.getCompanyName().replaceAll("\\W", "_");
        if (companyName.length() > 8) {
            companyName = companyName.substring(0, 8);
        }

        final File file = fileDownload.createTmpFile("performance_" + companyName, "pdf");

        final OutputStream out = new FileOutputStream(file);
        try {
            reportBuilder.createPerformanceReport(bean, user, out);
        } finally {
            out.close();
        }
        return file;
    }


    /**
     * @param s
     * @param extension
     * @return
     * @throws IOException
     */
    private File createTmpFile(final Shipment s, final String extension) throws IOException {
        final String fileName = createFileName(s, extension);
        //create tmp file with report PDF content.
        return fileDownload.createTmpFile(fileName, extension);
    }

    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/emailShipmentReport/{authToken}", method = RequestMethod.POST)
    public JsonObject emailShipmentReport(@PathVariable final String authToken,
            final @RequestBody JsonObject jsonRequest) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final ReportsSerializer serializer = getSerializer();
            final EmailShipmentReportRequest req = serializer.parseEmailShipmentReportRequest(jsonRequest);

            //check parameters
            if (req.getSn() == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Should be specified sn and trip request parameters");
            }

            final Shipment s = shipmentDao.findBySnTrip(user.getCompany(), req.getSn(), req.getTrip());
            if (s == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found shipment for " + req.getSn() + " (" + req.getTrip()
                        + ") for given company");
            }

            final File file = createShipmentReport(s, user);
            final Set<String> emails = new HashSet<>();

            //add users
            for (final User u: userDao.findAll(req.getUsers())) {
                emails.add(u.getEmail());
            }
            //add emails
            emails.addAll(req.getEmails());

            emailService.sendMessage(emails.toArray(new String[emails.size()]),
                    req.getSubject(), req.getMessageBody(), file);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to save shipment by request: " + jsonRequest, e);
            return createErrorResponse(e);
        }
    }

    /**
     * @return
     */
    private ReportsSerializer getSerializer() {
        return new ReportsSerializer();
    }

    @RequestMapping(value = "/demoOptimizer/{authToken}", method = RequestMethod.GET)
    public void demoOptimizer(@PathVariable final String authToken,
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new IOException("Should be specified shipmentId or (sn and trip) request parameters");
        }

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Shipment s;
            if (shipmentId != null) {
                s = shipmentDao.findOne(shipmentId);
            } else {
                s = shipmentDao.findBySnTrip(user.getCompany(), sn, trip);
            }

            checkCompanyAccess(user, s);
            if (s == null) {
                throw new FileNotFoundException("Unknown shipment: " + sn + "(" + trip + ")");
            }

            log.debug("Get readings from DB");
            //get readings from DB
            final List<ShortTrackerEvent> readings = new LinkedList<>();
            final List<TrackerEvent> events = trackerEventDao.getEvents(s);
            for (final TrackerEvent e : events) {
                if (e.getLatitude() != null && e.getLongitude() != null) {
                    readings.add(new ShortTrackerEvent(e));
                }
            }

            //create picture.
            final BufferedImage image;

            log.debug("Create image with not optimized readings");
            final BufferedImage im1 = createMapWithPath(readings);
            log.debug("Create image with optimized readings");
            final BufferedImage im2 = createMapWithPath(this.eventsOptimizer.optimize(readings));

            log.debug("Create composite image");
            image = new BufferedImage(
                    im1.getWidth() + im2.getWidth(),
                    Math.max(im1.getHeight(), im2.getHeight()),
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = image.createGraphics();
            try {
                g.drawImage(im1, 0, 0, null);
                g.drawImage(im2, im1.getWidth(), 0, null);

                //paint separator.
                g.setStroke(new BasicStroke(3f));
                g.setColor(Color.BLACK);
                g.drawLine(im1.getWidth(), 0, im1.getWidth(), image.getHeight());
            } finally {
                g.dispose();
            }

            log.debug("Write image to file");
            //write image to file
            final File file = createTmpFile(s, "gif");
            ImageIO.write(image, "gif", file);

            log.debug("Do redirect");
            final int index = request.getRequestURL().indexOf("/demoOptimizer");
            response.sendRedirect(FileDownloadController.createDownloadUrl(request.getRequestURL().substring(0, index),
                    authToken, file.getName()));

        } catch (final Exception e) {
            log.error("Failed to create optimizer demo", e);
            throw e;
        }
    }

    /**
     * @return
     */
    @SuppressWarnings("serial")
    private BufferedImage createMapWithPath(final List<ShortTrackerEvent> readings) {
        final ShipmentReportBean bean = new ShipmentReportBean();
        bean.setDeviceColor(Color.RED);
        bean.getReadings().addAll(readings);

        final Rectangle viewArea = new Rectangle(612, 612);


        //use image buffer for avoid of problems with alpha chanel.
        final BufferedImage image = new BufferedImage(viewArea.width, viewArea.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();

        try {
            new MapRendererImpl(bean) {
                @Override
                protected List<ShortTrackerEvent> optimize(final List<ShortTrackerEvent> readings) {
                    return readings;
                };
            }.render(g, viewArea);
        } finally {
            g.dispose();
        }

        return image;
    }

    /**
     * @param s shipment.
     * @param extension file extension.
     * @return
     */
    private String createFileName(final Shipment s, final String extension) {
        final StringBuilder sb = new StringBuilder(Device.getSerialNumber(s.getDevice().getImei()));
        //add shipment trip count
        sb.append('(');
        sb.append(s.getTripCount());
        sb.append(')');
        sb.insert(0, "shipment-");
        return sb.toString();
    }

    /**
     * @param reportBuilder the reportBuilder to set
     */
    public void setReportBuilder(final PdfReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }
    /**
     * @return the reportBuilder
     */
    public PdfReportBuilder getReportBuilder() {
        return reportBuilder;
    }
}
