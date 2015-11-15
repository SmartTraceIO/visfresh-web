/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.SingleShipmentDto;
import com.visfresh.io.SingleShipmentTimeItem;
import com.visfresh.io.UserResolver;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.services.lists.ListShipmentItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Shipment")
@RequestMapping("/rest")
public class ShipmentController extends AbstractController implements ShipmentConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);
    /**
     * Report service.
     */
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private ReferenceResolver referenceResolver;
    @Autowired
    private UserResolver userResolver;

    /**
     * Default constructor.
     */
    public ShipmentController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param shipment shipment.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveShipment(@PathVariable final String authToken,
            final @RequestBody String shipment) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipment(user);

            final SaveShipmentRequest req = getSerializer(user).parseSaveShipmentRequest(getJSonObject(shipment));
            checkCompanyAccess(user, req.getShipment());

            req.getShipment().setCompany(user.getCompany());
            final Long id = shipmentDao.save(req.getShipment()).getId();

            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = createShipmentTemplate(
                        user.getCompany(), req.getShipment(), req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(getSerializer(user).toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    private Long createShipmentTemplate(final Company company, final Shipment shipment, final String templateName) {
        final ShipmentTemplate tpl = new ShipmentTemplate(shipment);
        tpl.setCompany(company);
        tpl.setAddDateShipped(true);
        tpl.setDetectLocationForShippedFrom(true);
        tpl.setUseCurrentTimeForDateShipped(true);
        tpl.setName(templateName);
        return shipmentTemplateDao.save(tpl).getId();
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipments(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final boolean onlyWithAlerts,
            @RequestParam(required = false) final Long shippedFrom,
            @RequestParam(required = false) final Long shippedTo,
            @RequestParam(required = false) final String goods,
            @RequestParam(required = false) final String device,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final ShipmentSerializer ser = getSerializer(user);

            final Filter filter = createFilter(onlyWithAlerts, shippedFrom, shippedTo, goods, device, status);
            final List<ListShipmentItem> shipments = getShipments(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultListShipmentsSortingOrder()),
                    filter,
                    page);
            final int total = shipmentDao.getEntityCount(user.getCompany(), filter);

            final JsonArray array = new JsonArray();
            for (final ListShipmentItem s : shipments) {
                array.add(ser.toJson(s));
            }
            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            PROPERTY_SHIPMENT_DESCRIPTION,
            PROPERTY_SHIPMENT_ID,
            PROPERTY_ALERT_PROFILE_ID,
            PROPERTY_SHIPPED_FROM,
            PROPERTY_SHIPPED_TO,
            PROPERTY_COMMENTS_FOR_RECEIVER,
            PROPERTY_ALERT_PROFILE
        };
    }
    /**
     * @param onlyWithAlerts
     * @param shippedFrom
     * @param shippedTo
     * @param goods
     * @param device
     * @param status
     * @return
     */
    private Filter createFilter(final boolean onlyWithAlerts, final Long shippedFrom,
            final Long shippedTo, final String goods, final String device, final String status) {
        final Filter f = new Filter();
        if (shippedFrom != null) {
            f.addFilter(PROPERTY_SHIPPED_FROM, shippedFrom);
        }
        if (shippedTo != null) {
            f.addFilter(PROPERTY_SHIPPED_TO, shippedTo);
        }
        if (goods != null) {
            f.addFilter(PROPERTY_SHIPMENT_DESCRIPTION, goods);
        }
        if (device != null) {
            f.addFilter(PROPERTY_DEVICE_IMEI, device);
        }
        if (status != null) {
            f.addFilter(PROPERTY_STATUS, ShipmentStatus.valueOf(status));
        }
        return f;
    }
    /**
     * @param company
     * @return
     */
    private List<ListShipmentItem> getShipments(final Company company,
            final Sorting sorting,
            final Filter filter,
            final Page page) {
        final List<Shipment> shipments = shipmentDao.findByCompany(company, sorting, page, filter);
        final List<ListShipmentItem> result = new LinkedList<ListShipmentItem>();
        //add alerts to each shipment.
        for (final Shipment s : shipments) {
            final List<Alert> alerts = alertDao.getAlerts(s,
                    new Date(0L), new Date(System.currentTimeMillis() + 100000000l));
            final ListShipmentItem dto = new ListShipmentItem(s);
            dto.getAlertSummary().putAll(toSummaryMap(alerts));
            result.add(dto);
        }
        return result;
    }

    /**
     * @param authToken authentication token.
     * @param shipmentId shipment ID.
     * @return shipment.
     */
    @RequestMapping(value = "/getShipment/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final Shipment shipment = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, shipment);

            return createSuccessResponse(getSerializer(user).toJson(shipment));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private ShipmentSerializer getSerializer(final User user) {
        final ShipmentSerializer s = new ShipmentSerializer(user);
        s.setReferenceResolver(referenceResolver);
        s.setUserResolver(userResolver);
        return s;
    }
    @RequestMapping(value = "/deleteShipment/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String deleteShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipment(user);

            final Shipment s = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, s);

            shipmentDao.delete(s);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }

    @RequestMapping(value = "/getSingleShipment/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentData(@PathVariable final String authToken,
            @RequestParam final String fromDate,
            @RequestParam final String toDate,
            @RequestParam final Long shipment) {

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentData(user);

            final ShipmentSerializer ser = getSerializer(user);
            final Date startDate = ser.parseDate(fromDate);
            final Date endDate = ser.parseDate(toDate);

            final Shipment s = shipmentDao.findOne(shipment);
            checkCompanyAccess(user, s);
            if (s == null) {
                return null;
            }

            final SingleShipmentDto dto = creatSingleShipmentDto(s);

            final List<TrackerEvent> events = trackerEventDao.getEvents(s, startDate, endDate);
            for (final TrackerEvent e : events) {
                final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
                item.setEvent(e);
                dto.getItems().add(item);
            }
            Collections.sort(dto.getItems());

            if (events.size() > 0) {
                //add alerts
                final List<Alert> alerts = alertDao.getAlerts(s, startDate, endDate);
                for (final Alert alert : alerts) {
                    final SingleShipmentTimeItem item = getBestCandidate(dto.getItems(), alert.getDate());
                    item.getAlerts().add(alert);
                }

                //add arrivals
                final List<Arrival> arrivals = arrivalDao.getArrivals(s, startDate, endDate);
                for (final Arrival arrival : arrivals) {
                    final SingleShipmentTimeItem item = getBestCandidate(dto.getItems(), arrival.getDate());
                    item.getArrivals().add(arrival);
                }

                dto.getAlertSummary().putAll(toSummaryMap(alerts));
            }

            return createSuccessResponse(dto == null ? null : ser.toJson(dto));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param alerts
     * @return
     */
    public static  Map<AlertType, Integer> toSummaryMap(
            final List<Alert> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final Alert alert : alerts) {
            Integer numAlerts = map.get(alert.getType());
            if (numAlerts == null) {
                numAlerts = 0;
            }
            numAlerts = numAlerts + 1;
            map.put(alert.getType(), numAlerts);
        }
        return map;
    }
    /**
     * @param items
     * @param date
     * @return
     */
    private SingleShipmentTimeItem getBestCandidate(final List<SingleShipmentTimeItem> items, final Date date) {
        for (final SingleShipmentTimeItem i : items) {
            if (i.getEvent().getTime().equals(date) || i.getEvent().getTime().after(date)) {
                return i;
            }
        }
        return items.get(items.size() - 1);
    }

    /**
     * @param shipment
     * @return
     */
    private SingleShipmentDto creatSingleShipmentDto(final Shipment shipment) {
        return new SingleShipmentDto(shipment);
    }
}
