/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.io.ReportSerializer;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.SingleShipmentDto;
import com.visfresh.services.ReportService;
import com.visfresh.services.RestService;
import com.visfresh.services.lists.ListShipmentItem;
import com.visfresh.services.lists.ListShipmentTemplateItem;

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
     * REST service.
     */
    @Autowired
    private RestService restService;
    /**
     * Report service.
     */
    @Autowired
    private ReportService reportService;

    /**
     * Default constructor.
     */
    public ShipmentController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param tpl shipment template.
     * @return ID of saved shipment template.
     */
    @RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveShipmentTemplate(@PathVariable final String authToken,
            final @RequestBody String tpl) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipmentTemplate(user);

            final Long id = restService.saveShipmentTemplate(
                    user.getCompany(), getSerializer(user).parseShipmentTemplate(getJSonObject(tpl)));
            return createIdResponse("shipmentTemplateId", id);
        } catch (final Exception e) {
            log.error("Failed to save shipment template", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipment templates.
     */
    @RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final List<ShipmentTemplate> tpls = restService.getShipmentTemplates(user.getCompany());
            sort(tpls);

            final List<ShipmentTemplate> templates = getPage(tpls, pageIndex, pageSize);

            final JsonArray array = new JsonArray();
            final EntityJSonSerializer ser = getSerializer(user);
            for (final ShipmentTemplate tpl : templates) {
                final ListShipmentTemplateItem item = new ListShipmentTemplateItem(tpl);
                array.add(ser.toJson(item));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param tpls
     */
    private void sort(final List<ShipmentTemplate> tpls) {
        // TODO Auto-generated method stub
        sortById(tpls, true);
    }

    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/getShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final ShipmentTemplate template = restService.getShipmentTemplate(user.getCompany(), shipmentTemplateId);
            return createSuccessResponse(getSerializer(user).toJson(template));
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/deleteShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String deleteShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipmentTemplate(user);

            restService.deleteShipmentTemplate(user.getCompany(), shipmentTemplateId);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete shipment templates", e);
            return createErrorResponse(e);
        }
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
            final Long id = restService.saveShipment(user.getCompany(), req.getShipment());

            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = restService.createShipmentTemplate(
                        user.getCompany(), req.getShipment(), req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(getSerializer(user).toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipments(@PathVariable final String authToken,
            @RequestParam final int pageIndex,
            @RequestParam final int pageSize,
            @RequestParam(required = false) final boolean onlyWithAlerts,
            @RequestParam(required = false) final String shippedFrom,
            @RequestParam(required = false) final String shippedTo,
            @RequestParam(required = false) final String goods,
            @RequestParam(required = false) final String device,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final ReportSerializer ser = getReportSerializer(user);

            final List<ListShipmentItem> shipments = getPage(restService.getShipments(user.getCompany()), pageIndex, pageSize);
            sort(shipments, sc, so);

            final JsonArray array = new JsonArray();
            for (final ListShipmentItem t : shipments) {
                //TODO move filtering to DAO
                if (onlyWithAlerts && !hasAlerts(t)) {
                    continue;
                }
//                if (shippedFrom != null && (t.getShippedFrom() == null
//                        || !shippedFrom.equals(t.getShippedFrom()))) {
//                    continue;
//                }
//                if (shippedTo != null && (t.getShippedTo() == null
//                        || !shippedTo.equals(t.getShippedTo()))) {
//                    continue;
//                }
                if (device != null && (t.getDeviceSN() == null
                        || !device.equals(t.getDeviceSN()))) {
                    continue;
                }
                if (goods != null && (t.getShipmentDescription() == null
                        || t.getShipmentDescription().indexOf(goods) < 0)) {
                    continue;
                }
                if (status != null && !t.getStatus().toString().equals(status)) {
                    continue;
                }

                array.add(ser.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
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

            final Shipment shipment = restService.getShipment(user.getCompany(), shipmentId);
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

            restService.deleteShipment(user.getCompany(), shipmentId);
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

            final SingleShipmentDto dto = reportService.getSingleShipment(startDate, endDate, shipment);
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
}
