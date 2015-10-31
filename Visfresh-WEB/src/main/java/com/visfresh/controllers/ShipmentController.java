/**
 *
 */
package com.visfresh.controllers;

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
import com.visfresh.io.ReportSerializer;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentStateDto;
import com.visfresh.io.SingleShipmentDto;
import com.visfresh.services.ReportService;
import com.visfresh.services.RestService;

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

            final List<ShipmentTemplate> templates = getPage(
                    restService.getShipmentTemplates(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final ShipmentTemplate tpl : templates) {
                array.add(getSerializer(user).toJson(tpl));
            }

            return createSuccessResponse(array);
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
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final ReportSerializer ser = getReportSerializer(user);

            final List<ShipmentStateDto> shipments = getPage(restService.getShipments(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final ShipmentStateDto t : shipments) {
                array.add(ser.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
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

            final Date startDate = parseDate(fromDate);
            final Date endDate = parseDate(toDate);

            final SingleShipmentDto dto = reportService.getSingleShipment(startDate, endDate, shipment);
            return createSuccessResponse(dto == null ? null : getReportSerializer(user).toJson(dto));
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
        return new ReportSerializer(user.getTimeZone());
    }
}
