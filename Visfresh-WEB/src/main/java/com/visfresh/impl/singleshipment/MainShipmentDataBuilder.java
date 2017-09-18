/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.entities.ShipmentStatus;
import com.visfresh.impl.services.NotificationServiceImpl;
import com.visfresh.impl.services.SingleShipmentServiceImpl;
import com.visfresh.io.json.ShipmentSessionSerializer;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MainShipmentDataBuilder implements SingleShipmentPartBuilder {
    protected final NamedParameterJdbcTemplate jdbc;
    protected final Long shipmentId;

    private final Map<Long, SingleShipmentBean> beans = new HashMap<>();
    private final Map<Long, ShipmentSession> sessions = new HashMap<>();
    private final ShipmentSessionSerializer sessionSerializer = new ShipmentSessionSerializer();

    /**
     * @param jdbc JDBC template.
     * @param shipmentId shipment ID.
     */
    public MainShipmentDataBuilder(final NamedParameterJdbcTemplate jdbc, final Long shipmentId) {
        super();
        this.jdbc = jdbc;
        this.shipmentId = shipmentId;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#getPriority()
     */
    @Override
    public int getPriority() {
        return MAX_PRIORITY; //highest priority;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#build(com.visfresh.impl.singleshipment.SingleShipmentBuildContext)
     */
    @Override
    public void build(final SingleShipmentBuildContext context) {
        final SingleShipmentBean mainBean = beans.remove(shipmentId);
        if (mainBean == null) {
            return;
        }

        //create result data
        final SingleShipmentData data = new SingleShipmentData();
        context.setData(data);
        data.setBean(mainBean);

        final Set<Long> allSiblingIds = new HashSet<>();
        allSiblingIds.add(mainBean.getShipmentId());

        //add sibling beans to data.
        for (final SingleShipmentBean sibling : beans.values()) {
            data.getSiblings().add(sibling);
            allSiblingIds.add(sibling.getShipmentId());
        }

        //add siblings to each bean.
        addSiblingIds(mainBean, allSiblingIds);
        for (final SingleShipmentBean sibling : beans.values()) {
            addSiblingIds(sibling, allSiblingIds);
        }
    }

    /**
     * @param bean
     * @param originIds
     */
    private void addSiblingIds(final SingleShipmentBean bean, final Set<Long> originIds) {
        for (final Long id : originIds) {
            if (!id.equals(bean.getShipmentId())) {
                bean.getSiblings().add(id);
            }
        }
    }

    /**
     * Clears the builder.
     */
    private void clear() {
        beans.clear();
        sessions.clear();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#fetchData()
     */
    @Override
    public void fetchData() {
        clear();

        final String query = buildQuery();
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipmentId);

        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);
        for (final Map<String, Object> row : rows) {
            processRow(row);
        }
    }

    /**
     * @return
     */
    private String buildQuery() {
        return "select  "
            + " s.id as id"
            + ", s.noalertsifcooldown as alertSuppressionMinutes"
            + ", s.arrivalnotifwithIn as arrivalNotificationWithinKm"
            + ", s.arrivaldate as arrivalDate"
            + ", s.assetnum as assetNum"
            + ", s.assettype as assetType"
            + ", s.comments as commentsForReceiver"
            + ", s.company as company"
            + ", s.device as device"
            + ", d.color as deviceColor"
            + ", d.name as deviceName"
            + ", s.nonotifsifnoalerts as excludeNotificationsIfNoAlerts"
            + ", s.status as status"
            + ", s.tripcount as tripCount"
            + ", d.tripcount as deviceTripCount"
            + ", s.noalertsafterarrivalminutes as noAlertsAfterArrivalMinutes"
            + ", s.noalertsafterstartminutes as noAlertsAfterStartMinutes"
            + ", s.palletid as palletId"
            + ", s.arrivalreport as isSendArrivalReport"
            + ", s.arrivalreportonlyifalerts as sendArrivalReportOnlyIfAlerts"
            + ", s.description as description"
            + ", s.id as id"
            + ", s.isautostart as autostart"
            + ", s.shutdownafterstartminutes as shutDownAfterStartMinutes"
            + ", s.shutdownafterarrivalminutes as shutdownDeviceAfterMinutes"
            + ", s.shipmentdate as startTime"
            + ", s.eta as eta"
            + ", ss.state as session"
            + " from shipments s"
            + " left outer join shipmentsessions ss on ss.shipment = s.id"
            + " join devices d on d.imei = s.device"
            + " where s.id = :shipment or "
            + " s.siblings = :shipment or s.siblings like concat(:shipment, ',%')"
            + " or s.siblings like concat('%,', :shipment, ',%') or s.siblings"
            + " like concat('%,', :shipment)";
    }

    /**
     * @param row
     */
    private void processRow(final Map<String, Object> row) {
        final SingleShipmentBean bean = new SingleShipmentBean();
        final ShipmentSession session = getSession(row);

        if (session != null) {
            final Date alertsSuppressedTime = session.getAlertsSuppressionDate();
            if (alertsSuppressedTime != null || session.isAlertsSuppressed()) {
                bean.setAlertsSuppressed(true);
                bean.setAlertsSuppressionTime(alertsSuppressedTime);
            }
            bean.setArrivalReportSent(NotificationServiceImpl.isArrivalReportSent(session));
        }

        bean.setAlertSuppressionMinutes(asInteger(row.get("alertSuppressionMinutes")));

        bean.setArrivalNotificationWithinKm(asInteger(row.get("arrivalNotificationWithinKm")));
        bean.setArrivalTime((Date) row.get("arrivalDate"));
        bean.setAssetNum((String) row.get("assetNum"));
        bean.setAssetType((String) row.get("assetType"));

        bean.setCommentsForReceiver((String) row.get("commentsForReceiver"));
        bean.setCompanyId(asLong(row.get("company")));

        bean.setDevice((String) row.get("device"));
        bean.setDeviceColor((String) row.get("deviceColor"));
        bean.setDeviceName((String) row.get("deviceName"));
        bean.setExcludeNotificationsIfNoAlerts(Boolean.TRUE.equals(row.get("excludeNotificationsIfNoAlerts")));

        bean.setStatus(ShipmentStatus.valueOf((String) row.get("status")));
        bean.setTripCount(asInteger(row.get("tripCount")));

        bean.setLatestShipment(bean.getTripCount() >= asInteger(row.get("deviceTripCount"))
                && bean.getStatus() != ShipmentStatus.Ended);
        bean.setNoAlertsAfterArrivalMinutes(asInteger(row.get("noAlertsAfterArrivalMinutes")));
        bean.setNoAlertsAfterStartMinutes(asInteger(row.get("noAlertsAfterStartMinutes")));
        bean.setPalletId((String) row.get("palletId"));

        bean.setSendArrivalReport(Boolean.TRUE.equals(row.get("isSendArrivalReport")));
        bean.setSendArrivalReportOnlyIfAlerts(Boolean.TRUE.equals(row.get("sendArrivalReportOnlyIfAlerts")));
        bean.setShipmentDescription((String) row.get("description"));
        bean.setShipmentId(asLong(row.get("id")));
        bean.setShipmentType(Boolean.TRUE.equals(row.get("autostart")) ? "Autostart": "Manual");
        bean.setShutDownAfterStartMinutes(asInteger(row.get("shutDownAfterStartMinutes")));
        bean.setShutdownDeviceAfterMinutes(asInteger(row.get("shutdownDeviceAfterMinutes")));
        bean.setStartTime((Date) row.get("startTime"));
        final Date eta = (Date) row.get("eta");
        if (eta != null) {
            bean.setPercentageComplete(SingleShipmentServiceImpl.getPercentageCompleted(
                    bean.getStartTime(), new Date(), eta));
            bean.setEta(eta);
        }

        beans.put(bean.getShipmentId(), bean);
        sessions.put(bean.getShipmentId(), session);
    }

    /**
     * @param row DB data row.
     * @return shipment session from row.
     */
    private ShipmentSession getSession(final Map<String, Object> row) {
        final String state = (String) row.get("session");
        if (state == null) {
            return null;
        }

        final ShipmentSession session = sessionSerializer.parseSession(state);
        session.setShipmentId(shipmentId);
        return session;
    }
}
