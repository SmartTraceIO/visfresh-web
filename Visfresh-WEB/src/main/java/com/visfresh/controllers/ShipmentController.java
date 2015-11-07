/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.ReportSerializer;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.SingleShipmentDto;
import com.visfresh.io.SingleShipmentTimeItem;
import com.visfresh.services.lists.ListShipmentItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Shipment")
@RequestMapping("/rest")
public class ShipmentController extends AbstractController {
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
            @RequestParam(required = false) final String shippedFrom,
            @RequestParam(required = false) final String shippedTo,
            @RequestParam(required = false) final String goods,
            @RequestParam(required = false) final String device,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final int page = pageIndex == null ? 1 : pageIndex.intValue();
        final int size = pageSize == null ? Integer.MAX_VALUE : pageSize.intValue();

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final ReportSerializer ser = getReportSerializer(user);

            final List<ListShipmentItem> shps = getShipments(user.getCompany());

            final Iterator<ListShipmentItem> iter = shps.iterator();
            while (iter.hasNext()) {
                final ListShipmentItem t = iter.next();
                if (onlyWithAlerts && !hasAlerts(t)
//                if (shippedFrom != null && (t.getShippedFrom() == null
//                        || !shippedFrom.equals(t.getShippedFrom()))) {
//                    continue;
//                }
//                if (shippedTo != null && (t.getShippedTo() == null
//                        || !shippedTo.equals(t.getShippedTo()))) {
//                    continue;
//                }
                || device != null && (t.getDeviceSN() == null || !device.equals(t.getDeviceSN()))
                || goods != null && (t.getShipmentDescription() == null
                    || t.getShipmentDescription().indexOf(goods) < 0)
                || status != null && !t.getStatus().toString().equals(status)) {
                    iter.remove();
                }
            }

            final int total = shps.size();
            final List<ListShipmentItem> shipments = getPage(shps, page, size);
            sort(shipments, sc, so);

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
     * @param company
     * @return
     */
    private List<ListShipmentItem> getShipments(final Company company) {
        final List<Shipment> shipments = shipmentDao.findByCompany(company);
        final List<ListShipmentItem> result = new LinkedList<ListShipmentItem>();
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
     * @param profiles
     * @param sc
     * @param so
     */
    private void sort(final List<ListShipmentItem> profiles, final String sc, final String so) {
        final boolean ascent = !"desc".equals(so);
        Collections.sort(profiles, new Comparator<ListShipmentItem>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final ListShipmentItem o1, final ListShipmentItem o2) {
                if ("shipmentId".equals(sc)) {
                    return compareTo(o1.getShipmentId(), o2.getShipmentId(), ascent);
                }
                if ("status".equals(sc)) {
                    return compareTo(o1.getStatus().toString(), o2.getStatus().toString(), ascent);
                }
                if ("deviceSN".equals(sc)) {
                    compareTo(o1.getDeviceSN(), o2.getDeviceSN(), ascent);
                }
                if ("deviceName".equals(sc)) {
                    return compareTo(o1.getDeviceName(), o2.getDeviceName(), ascent);
                }
                if ("tripCount".equals(sc)) {
                    return compareTo(o1.getTripCount(), o2.getTripCount(), ascent);
                }
                if ("shipmentDescription".equals(sc)) {
                    return compareTo(o1.getShipmentDescription(), o2.getShipmentDescription(), ascent);
                }
                if ("palletId".equals(sc)) {
                    return compareTo(o1.getPalettId(), o2.getPalettId(), ascent);
                }
                if ("assetNum".equals(sc)) {
                    return compareTo(o1.getAssetNum(), o2.getAssetNum(), ascent);
                }
                if ("assetType".equals(sc)) {
                    return compareTo(o1.getAssetType(), o2.getAssetType(), ascent);
                }
                if ("shippedFrom".equals(sc)) {
                    return compareTo(o1.getShippedFrom(), o2.getShippedFrom(), ascent);
                }
                if ("shipmentDate".equals(sc)) {
                    return compareTo(o1.getShipmentDate(), o2.getShipmentDate(), ascent);
                }
                if ("shippedTo".equals(sc)) {
                    return compareTo(o1.getShippedTo(), o2.getShippedTo(), ascent);
                }
                if ("estArrivalDate".equals(sc)) {
                    return compareTo(o1.getEstArrivalDate(), o2.getEstArrivalDate(), ascent);
                }
                if ("actualArrivalDate".equals(sc)) {
                    return compareTo(o1.getActualArrivalDate(), o2.getActualArrivalDate(), ascent);
                }
                if ("percentageComplete".equals(sc)) {
                    return compareTo(o1.getPercentageComplete(), o2.getPercentageComplete(), ascent);
                }
                if ("alertProfileId".equals(sc)) {
                    return compareTo(o1.getAlertProfileId(), o2.getAlertProfileId(), ascent);
                }
                if ("alertProfileName".equals(sc)) {
                    return compareTo(o1.getAlertProfileName(), o2.getAlertProfileName(), ascent);
                }

                return compareTo(o1.getId(), o2.getId(), ascent);
            }
        });
    }
    /**
     * @param t
     * @return
     */
    private boolean hasAlerts(final ListShipmentItem t) {
        for (final Integer n : t.getAlertSummary().values()) {
            if (n != null && n > 0) {
                return true;
            }
        }
        return false;
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

            final ReportSerializer ser = getReportSerializer(user);
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
     * @param user
     * @return
     */
    protected ReportSerializer getReportSerializer(final User user) {
        return new ReportSerializer(user);
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
