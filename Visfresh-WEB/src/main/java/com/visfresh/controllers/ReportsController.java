/**
 *
 */
package com.visfresh.controllers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.visfresh.reports.geomap.AbstractGeoMapBuiler;
import com.visfresh.reports.geomap.GoogleGeoMapBuiler;
import com.visfresh.reports.geomap.OpenStreetMapBuilder;
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
    private AbstractGeoMapBuiler builder = new GoogleGeoMapBuiler();
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

        //create tmp file with report PDF content.
        final File file = fileDownload.createTmpFile(createFileName(s, "pdf"));

        final OutputStream out = new FileOutputStream(file);
        try {
            reportBuilder.createShipmentReport(bean, user, out);
        } finally {
            out.close();
        }
        return file;
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

            log.debug("Calculate the best zoom");
            final Rectangle viewArea = new Rectangle(250, 250);
            final int zoom = builder.calculateZoom(getCoordinates(readings),
                    new Dimension(viewArea.width, viewArea.height), 10);

            //create picture.
            final BufferedImage image;

            log.debug("Create image with not optimized readings");
            final BufferedImage im1 = createMapWithPath(readings, zoom);
            log.debug("Create image with optimized readings");
            final BufferedImage im2 = createMapWithPath(this.eventsOptimizer.optimize(readings), zoom);

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
            final File file = fileDownload.createTmpFile(createFileName(s, "gif"));
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
     * @param readings
     * @return
     */
    private List<Point2D> getCoordinates(final List<ShortTrackerEvent> readings) {
        final List<Point2D> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : readings) {
            coords.add(new Point2D.Double(e.getLongitude(), e.getLatitude()));
        }
        return coords;
    }

    /**
     * @return
     */
    private BufferedImage createMapWithPath(final List<ShortTrackerEvent> readings, final int zoom) {
        final Rectangle viewArea = new Rectangle(612, 612);

        final List<Point2D> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : readings) {
            coords.add(new Point2D.Double(e.getLongitude(), e.getLatitude()));
        }

        final int width = (int) Math.floor(viewArea.getWidth());
        final int height = (int) Math.floor(viewArea.getHeight());

        final Rectangle r = builder.getMapBounds(coords, zoom);

        final Point p = new Point(
                (int) (r.getX() - (viewArea.getWidth() - r.getWidth()) / 2.),
                (int) (r.getY() - (viewArea.getHeight() - r.getHeight()) / 2.));

        //use image buffer for avoid of problems with alpha chanel.
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();

        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            final Composite comp = g.getComposite();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            //set transparency before draw map
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));

            log.debug("Paint map");
            builder.paint(g, p, zoom, width, height);

            //restore transparency
            g.setComposite(comp);

            log.debug("Paint path");
            //create path shape
            final GeneralPath path = new GeneralPath();
            for (final ShortTrackerEvent e : readings) {
                final int x = Math.round(OpenStreetMapBuilder.lon2position(
                        e.getLongitude(), zoom) - p.x);
                final int y = Math.round(OpenStreetMapBuilder.lat2position(
                        e.getLatitude(), zoom) - p.y);
                final Point2D cp = path.getCurrentPoint();
                if (cp == null) {
                    path.moveTo(x, y);
                } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                    path.lineTo(x, y);
                }
            }

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.f));
            g.setColor(Color.RED);
            g.draw(path);

        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
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
        sb.append("." + extension);
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
