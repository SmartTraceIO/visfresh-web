/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.SingleShipmentBeanDao;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends ShipmentBaseDao<Shipment, Shipment> implements ShipmentDao {
    protected static final String PONUM_FIELD = "ponum";
    protected static final String TRIPCOUNT_FIELD = "tripcount";
    protected static final String PALETTID_FIELD = "palletid";
    protected static final String ASSETNUM_FIELD = "assetnum";
    public static final String SHIPMENTDATE_FIELD = "shipmentdate";
    protected static final String START_DATE = "startdate";
    protected static final String CRETED_BY = "createdby";
    public static final String ARRIVALDATE_FIELD = "arrivaldate";
    public static final String ETA_FIELD = "eta";
    protected static final String CUSTOMFIELDS_FIELD = "customfiels";
    public static final String STATUS_FIELD = "status";
    protected static final String DEVICE_FIELD = "device";
    protected static final String SIBLINGS_FIELD = "siblings";
    public static final String SIBLINGCOUNT_FIELD = "siblingcount";
    protected static final String ASSETTYPE_FIELD = "assettype";
    protected static final String LASTEVENT_FIELD = "lasteventdate";
    protected static final String DEVICESHUTDOWNDATE_FIELD = "deviceshutdowndate";

    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private SingleShipmentBeanDao shipmentBeanDao;

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super();
        propertyToDbFields.put(ShipmentConstants.ALERT_PROFILE_ID, ALERT_PROFILE_FIELD);
        propertyToDbFields.put(ShipmentConstants.ALERT_SUPPRESSION_MINUTES, NOALERTIFCOODOWN_FIELD);
        propertyToDbFields.put(ShipmentConstants.ARRIVAL_NOTIFICATION_WITHIN_KM,
                ARRIVALNOTIFWITHIN_FIELD);
        propertyToDbFields.put(ShipmentConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS,
                NONOTIFSIFNOALERTS_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_FROM, SHIPPEDFROM_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_TO, SHIPPEDTO_FIELD);
        propertyToDbFields.put(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_MINUTES,
                SHUTDOWNTIMEOUT_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES,
//                );
        propertyToDbFields.put(ShipmentConstants.COMMENTS_FOR_RECEIVER, COMMENTS_FIELD);
        propertyToDbFields.put(ShipmentConstants.SHIPMENT_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(ShipmentConstants.ALERT_PROFILE, ALERT_PROFILE_FIELD);
        propertyToDbFields.put(ShipmentConstants.DEVICE_IMEI, DEVICE_FIELD);
        propertyToDbFields.put(ShipmentConstants.STATUS, STATUS_FIELD);
        propertyToDbFields.put(ShipmentConstants.CUSTOM_FIELDS, CUSTOMFIELDS_FIELD);
        propertyToDbFields.put(ShipmentConstants.SHIPMENT_DATE, SHIPMENTDATE_FIELD);
        propertyToDbFields.put(ShipmentConstants.ARRIVAL_DATE, ARRIVALDATE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PO_NUM, PONUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.TRIP_COUNT, TRIPCOUNT_FIELD);
        propertyToDbFields.put(ShipmentConstants.ASSET_NUM, ASSETNUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.PALLET_ID, PALETTID_FIELD);
        propertyToDbFields.put(ShipmentConstants.SHIPMENT_ID, ID_FIELD);
        propertyToDbFields.put(ShipmentConstants.ASSET_TYPE, ASSETTYPE_FIELD);
        propertyToDbFields.put(ShipmentConstants.ETA, ETA_FIELD);
        propertyToDbFields.put(ShipmentConstants.SIBLING_COUNT, SIBLINGCOUNT_FIELD);
        propertyToDbFields.put(ShipmentConstants.LAST_READING_TIME, LASTEVENT_FIELD);
        propertyToDbFields.put(ShipmentConstants.LAST_READING_TIME_ISO, LASTEVENT_FIELD);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity()
     */
    @Override
    protected Shipment createEntity() {
        return new Shipment();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipments(com.visfresh.entities.Company)
     */
    @Override
    public List<Shipment> findActiveShipments(final Company company) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("company", company.getId());
        params.put("st1", ShipmentStatus.Ended.name());
        params.put("st2", ShipmentStatus.Arrived.name());

        final String sql = "select * from " + TABLE + " s"
                + " where"
                + " s." + STATUS_FIELD + "<> :st1"
                + " and s." + STATUS_FIELD + "<> :st2"
                + " and s." + ISTEMPLATE_FIELD + " = false"
                + " and s.company = :company"
                + " order by s." + ID_FIELD + " desc";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<Shipment> result = new LinkedList<Shipment>();

        for (final Map<String, Object> row : rows) {
            final Shipment s = createEntity(row);
            result.add(s);
            resolveReferences(s, row, cache);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findNextShipmentFor(com.visfresh.entities.Shipment)
     */
    @Override
    public Shipment findNextShipmentFor(final Shipment s) {
        //first of all find the last tracker event for given shipment
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());
        params.put("device", s.getDevice().getImei());

        final String sql = "select id from " + TABLE + " s"
                + " where"
                + " s." + ISTEMPLATE_FIELD + " = false"
                + " and s." + ID_FIELD + "> :shipment"
                + " and s." + DEVICE_FIELD + "= :device"
                + " order by s." + ID_FIELD + " limit 1";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        if (!rows.isEmpty()) {
            final Long nextShipmentId = ((Number) rows.get(0).get("id")).longValue();
            return findOne(nextShipmentId);
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipment(java.lang.String)
     */
    @Override
    public Shipment findLastShipment(final String imei) {
        //sorting
        final Sorting sort = new Sorting(false, ShipmentConstants.SHIPMENT_ID);
        //filter
        final Filter filter = new Filter();
        filter.addFilter(ShipmentConstants.DEVICE_IMEI, imei);
        //page
        final List<Shipment> shipments = findAll(filter, sort, new Page(1, 1));

        if (shipments.size() > 0) {
            return shipments.get(0);
        } else {
            return null;
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ArrivalDao#moveToNewDevice(com.visfresh.entities.Device, com.visfresh.entities.Device)
     */
    @Override
    public void moveToNewDevice(final Device oldDevice, final Device newDevice) {
        final String sql = "update " + TABLE + " set device = :new where device = :old";
        final Map<String, Object> params = new HashMap<>();
        params.put("old", oldDevice.getImei());
        params.put("new", newDevice.getImei());

        jdbc.update(sql, params);
        shipmentBeanDao.clearShipmentBeanForDevice(oldDevice.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getShipmentId(java.lang.String, int)
     */
    @Override
    public Long getShipmentId(final String sn, final int tripCount) {
        final Map<String, Object> map = new HashMap<>();
        map.put("sn", Device.addZeroSymbolsToSn(sn));
        map.put("trip", tripCount);

        final List<Map<String, Object>> list = jdbc.queryForList("select s.id as id from shipments s "
                + "where s.device like concat('%', :sn, '_') and s.tripcount = :trip", map);
        if (list.size() > 0) {
            return ((Number) list.get(0).get("id")).longValue();
        }

        return null;
    }

    /**
     * @param params
     * @return
     */
    protected Map<String, List<Alert>> getDeviceAlertMap(
            final Map<String, Object> params) {
        final String resultPrefix = "result_";

        //select alerts.
        final String selectAs = buildSelectAs(AlertDaoImpl.getFields(true), "a", resultPrefix);
        final String sql = "select " + selectAs + " from " + AlertDaoImpl.TABLE + " a, "
                + DeviceDaoImpl.TABLE + " d"
                + " where a." + AlertDaoImpl.DEVICE_FIELD + " = d." + DeviceDaoImpl.IMEI_FIELD
                + " and d." + DeviceDaoImpl.COMPANY_FIELD + " = :companyId"
                + " and a." + AlertDaoImpl.DATE_FIELD + " >= :startDate and a." + AlertDaoImpl.DATE_FIELD + " <= :endDate"
                + " order by a." + AlertDaoImpl.DATE_FIELD;

        //map of device alerts. The key is device ID.
        final Map<String, List<Alert>> deviceAlerts = new HashMap<String, List<Alert>>();

        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        for (final Map<String, Object> row : list) {
            final String deviceId = (String) row.get(resultPrefix + AlertDaoImpl.DEVICE_FIELD);
            //add alert to map
            List<Alert> alerts = deviceAlerts.get(deviceId);
            if (alerts == null) {
                alerts = new LinkedList<Alert>();
                deviceAlerts.put(deviceId, alerts);
            }

            alerts.add(AlertDaoImpl.createAlert(row));
        }

        return deviceAlerts;
    }

    /**
     * @param fields
     * @param tableAlias
     * @param resultPrefix
     * @return
     */
    private String buildSelectAs(final List<String> fields, final String tableAlias,
            final String resultPrefix) {
        final StringBuilder sb = new StringBuilder();
        for (final String f : fields) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(tableAlias + "." + f);
            sb.append(" as ");
            sb.append(resultPrefix + f);
        }
        return sb.toString();
    }

    /**
     * @param map
     * @param companyCache
     * @param deviceCache
     * @return
     */
    @Override
    protected Shipment createEntity(final Map<String, Object> map) {
        final Shipment e = super.createEntity(map);
        e.setPalletId((String) map.get(PALETTID_FIELD));
        e.setAssetNum((String) map.get(ASSETNUM_FIELD));
        e.setShipmentDate((Date) map.get(SHIPMENTDATE_FIELD));
        e.setStartDate((Date) map.get(START_DATE));
        e.setCreatedBy((String) map.get(CRETED_BY));
        e.setArrivalDate((Date) map.get(ARRIVALDATE_FIELD));
        e.setEta((Date) map.get(ETA_FIELD));
        e.setLastEventDate((Date) map.get(LASTEVENT_FIELD));
        e.setDeviceShutdownTime((Date) map.get(DEVICESHUTDOWNDATE_FIELD));
        e.getCustomFields().putAll(parseJsonMap((String) map.get(CUSTOMFIELDS_FIELD)));
        final Object statusObject = map.get(STATUS_FIELD);
        if (statusObject != null) {
            e.setStatus(ShipmentStatus.valueOf((String) statusObject));
        }
        e.setDevice(deviceDao.findOne((String) map.get(DEVICE_FIELD)));
        final String siblings = (String) map.get(SIBLINGS_FIELD);
        if (siblings != null) {
            for (final String sibling : siblings.split(",")) {
                e.getSiblings().add(Long.parseLong(sibling));
            }
        }
        e.setSiblingCount(((Number) map.get(SIBLINGCOUNT_FIELD)).intValue());
        e.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        e.setPoNum(((Number) map.get(PONUM_FIELD)).intValue());
        e.setAssetType((String) map.get(ASSETTYPE_FIELD));
        return e;
    }
    /**
     * @param str
     * @return
     */
    private Map<String, String> parseJsonMap(final String str) {
        return SerializerUtils.parseStringMap(SerializerUtils.parseJson(str));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final Shipment s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ISTEMPLATE_FIELD, false);
        params.put(PALETTID_FIELD, s.getPalletId());
        params.put(ASSETNUM_FIELD, s.getAssetNum());
        params.put(SHIPMENTDATE_FIELD, s.getShipmentDate());
        params.put(START_DATE, s.getStartDate());
        params.put(CRETED_BY, s.getCreatedBy());
        params.put(ARRIVALDATE_FIELD, s.getArrivalDate());
        params.put(ETA_FIELD, s.getEta());
        params.put(LASTEVENT_FIELD, s.getLastEventDate());
        params.put(DEVICESHUTDOWNDATE_FIELD, s.getDeviceShutdownTime());
        params.put(CUSTOMFIELDS_FIELD, SerializerUtils.toJson(s.getCustomFields()).toString());
        params.put(STATUS_FIELD, s.getStatus().name());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(TRIPCOUNT_FIELD, s.getTripCount());
        params.put(DEVICE_FIELD, s.getDevice() == null ? null : s.getDevice().getId());
        params.put(SIBLINGS_FIELD, s.getSiblings().isEmpty() ? null : StringUtils.combine(s.getSiblings(), ","));
        params.put(SIBLINGCOUNT_FIELD, s.getSiblingCount());
        params.put(ASSETTYPE_FIELD, s.getAssetType());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#save(com.visfresh.entities.ShipmentBase)
     */
    @Override
    public <S extends Shipment> S saveImpl(final S s) {
        final boolean insert = s.getId() == null;
        if (insert) {
            s.setTripCount(s.getDevice().getTripCount() + 1);
            s.getDevice().setTripCount(s.getTripCount());
        }
        final S result = super.saveImpl(s);
        if (insert) {
            deviceDao.save(s.getDevice());
        }
        shipmentBeanDao.clearShipmentBean(s.getId());
        return result;
    }
    @Override
    public List<Shipment> findActiveShipments(final String imei) {
        final List<Long> ids = findActiveShipmentIds(imei);

        final List<Shipment> result = new LinkedList<>();
        for (final Long id : ids) {
            result.add(findOne(id));
        }
        return result;
    }
    /**
     * @param imei
     * @return
     */
    private List<Long> findActiveShipmentIds(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);
        params.put("st1", ShipmentStatus.Ended.name());
        params.put("st2", ShipmentStatus.Arrived.name());

        final String sql = "select s." + ID_FIELD
                + " from " + TABLE + " s"
                + " where"
                + " s." + DEVICE_FIELD + "= :imei"
                + " and s." + STATUS_FIELD + "<> :st1"
                + " and s." + STATUS_FIELD + "<> :st2"
                + " order by s." + ID_FIELD + " desc";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        final List<Long> result = new LinkedList<Long>();
        for (final Map<String, Object> row : rows) {
            final Entry<String, Object> firstEntry = row.entrySet().iterator().next();
            result.add(((Number) firstEntry.getValue()).longValue());
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Shipment s, final Map<String, Object> row,
            final Map<String, Object> cache) {
        super.resolveReferences(s, row, cache);

        final String imei = (String) row.get(DEVICE_FIELD);
        final String key = "device_" + imei;
        Device device = (Device) cache.get(key);
        if (device == null) {
            device = deviceDao.findByImei(imei);
            cache.put(key, device);
        }

        s.setDevice(device);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getBySnTrip(java.lang.String, java.lang.Integer)
     */
    @Override
    public Shipment findBySnTrip(final Company company, final String sn, final Integer trip) {
        //create serial number filter
        final String key = "snKey";

        //build like clause
        final StringBuilder serialNum = new StringBuilder(sn);
        while (serialNum.length() < 6) {
            serialNum.insert(0, '0');
        }
        //append
        serialNum.insert(0, '%');
        serialNum.append('_');

        final Filter f = new Filter();
        if (company != null) {
            f.addFilter(COMPANY_FIELD, company.getId());
        }

        final DefaultCustomFilter customFilter = new DefaultCustomFilter();
        customFilter.addValue(key, serialNum.toString());
        customFilter.setFilter(DEVICE_FIELD + " like :" + key);
        f.addFilter(DEVICE_FIELD, customFilter);

        //add trip count filter
        f.addFilter(TRIPCOUNT_FIELD, trip);

        final List<Shipment> all = findAll(f, null, null);
        if (all.size() > 1) {
            throw new RuntimeException(all.size() + " shipments has selected for " + sn + "(" + trip + ")");
        }
        if (!all.isEmpty()) {
            return all.get(0);
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findAllBySnTrip(java.lang.String, int)
     */
    @Override
    public Shipment findBySnTrip(final String sn, final int trip) {
        return findBySnTrip(null, sn, trip);
    }
    /*
     * Changed the visibility to public.
     * @see com.visfresh.dao.impl.DaoImplBase#getSelectAllSupport()
     */
    @Override
    public SelectAllSupport getSelectAllSupport() {
        return super.getSelectAllSupport();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    public SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()){
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#addFiltes(com.visfresh.dao.Filter, java.util.Map, java.util.List, java.util.Map)
             */
            @Override
            protected void addFiltesForFindAll(final Filter filter, final Map<String, Object> params,
                    final List<String> filters) {
                final Object value = filter.getFilter(ShipmentConstants.ONLY_WITH_ALERTS);
                if (value != null) {
                    filter.removeFilter(ShipmentConstants.ONLY_WITH_ALERTS);
                }

                super.addFiltesForFindAll(filter, params, filters);

                if (Boolean.TRUE.equals(value)) {
                    filters.add("exists (select * from alerts where"
                            + " alerts.shipment = shipments.id and alerts.type <> 'LightOn' and alerts.type <> 'LightOff')");
                }
            }
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForFindAll()
             */
            @Override
            protected String buildSelectBlockForFindAll(final Filter filter) {
                String selectAll = "select "
                    + getTableName()
                    + ".*"
                    + " , substring(d." + DeviceDaoImpl.IMEI_FIELD + ", -7, 6)"
                    + " as " + ShipmentConstants.DEVICE_SN
                    + " , sfrom." + LocationProfileDaoImpl.NAME_FIELD
                    + " as " + ShipmentConstants.SHIPPED_FROM_LOCATION_NAME
                    + " , sto." + LocationProfileDaoImpl.NAME_FIELD
                    + " as " + ShipmentConstants.SHIPPED_TO_LOCATION_NAME
                    + " , te.temperature"
                    + " as " + ShipmentConstants.LAST_READING_TEMPERATURE
                    + " , (select count(*) from " + AlertDaoImpl.TABLE
                    + " al where al." + AlertDaoImpl.SHIPMENT_FIELD + " = " + TABLE + "." + ID_FIELD + ")"
                    + " as " + ShipmentConstants.ALERT_SUMMARY
                    + " , ap." + AlertProfileDaoImpl.UPPERTEMPLIMIT_FIELD + " as upperTemperatureLimit"
                    + " , ap." + AlertProfileDaoImpl.LOWERTEMPLIMIT_FIELD + " as lowerTemperatureLimit"
                    + " from " + getTableName()
                    + " left outer join " + AlertProfileDaoImpl.TABLE + " as ap"
                    + " on " + getTableName() + "." + ALERT_PROFILE_FIELD + " = ap.id"
                    + " left outer join " + DeviceDaoImpl.TABLE + " as d"
                    + " on " + getTableName() + "." + DEVICE_FIELD + " = d." + DeviceDaoImpl.IMEI_FIELD
                    + " left outer join " + LocationProfileDaoImpl.TABLE + " as sfrom"
                    + " on " + getTableName() + "." + SHIPPEDFROM_FIELD + " = sfrom." + LocationProfileDaoImpl.ID_FIELD
                    + " left outer join " + LocationProfileDaoImpl.TABLE + " as sto"
                    + " on " + getTableName() + "." + SHIPPEDTO_FIELD + " = sto." + LocationProfileDaoImpl.ID_FIELD
                    + " left outer join (select"
                    + " t." + TrackerEventDaoImpl.TEMPERATURE_FIELD + " as temperature,"
                    + " t." + TrackerEventDaoImpl.SHIPMENT_FIELD + " as shipment"
                    + " from " + TrackerEventDaoImpl.TABLE + " t"
                    + " join (select max(id) as id from " + TrackerEventDaoImpl.TABLE
                    + " group by " + TrackerEventDaoImpl.SHIPMENT_FIELD + ") t1"
                    + " on t1.id = t.id) te\n"
                    + "on te.shipment = " + getTableName() + "." + ID_FIELD + "\n";
                if (filter != null && Boolean.TRUE.equals(filter.getFilter(ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS))) {
                    selectAll += "\njoin (select max(id) as id, device as device from shipments group by device) newest on "
                            + TABLE + ".id = newest.id and "
                            + TABLE + ".device = newest.device\n";
                }

                return selectAll;
            }
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForEntityCount(com.visfresh.dao.Filter)
             */
            @Override
            protected String buildSelectBlockForEntityCount(final Filter filter) {
                String selectAll = super.buildSelectBlockForEntityCount(filter);
                if (filter != null && Boolean.TRUE.equals(filter.getFilter(ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS))) {
                    selectAll += "\njoin (select max(id) as id, device as device from shipments group by device) newest on "
                            + TABLE + ".id = newest.id and "
                            + TABLE + ".device = newest.device\n";
                }
                return selectAll;
            }
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#addSortForDbField(java.lang.String, java.util.List, boolean)
             */
            @Override
            protected void addSortForDbField(final String field, final List<String> sorts,
                    final boolean isAscent) {
                if (ShipmentConstants.DEVICE_SN.equals(field)) {
                    //also add the trip count to sort
                    super.addSortForDbField(field, sorts, isAscent);
                    super.addSortForDbField(TABLE + "." + TRIPCOUNT_FIELD, sorts, isAscent);
                } else if (ShipmentConstants.SHIPPED_FROM_LOCATION_NAME.equals(field)){
                    super.addSortForDbField(field, sorts, isAscent);
                } else if (ShipmentConstants.SHIPPED_TO_LOCATION_NAME.equals(field)){
                    super.addSortForDbField(field, sorts, isAscent);
                } else if (ShipmentConstants.LAST_READING_TEMPERATURE.equals(field)){
                    super.addSortForDbField(field, sorts, isAscent);
                } else if (ShipmentConstants.ALERT_SUMMARY.equals(field)){
                    super.addSortForDbField(field, sorts, isAscent);
                } else if (ShipmentConstants.SHIPPED_FROM_DATE.equals(field)){
                    //shipped from date
                    super.addSortForDbField("COALESCE(" + field
                            + "," + SHIPMENTDATE_FIELD + ")", sorts, isAscent);
                } else {
                    super.addSortForDbField(TABLE + "." + field, sorts, isAscent);
                }
            }
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.String, java.lang.Object, java.util.Map, java.util.List)
             */
            @Override
            protected void addFilterValue(final String property, final Object value, final Map<String, Object> params,
                    final List<String> filters) {
                final String defaultKey = DEFAULT_FILTER_KEY_PREFIX + property;

                if (STATUS_FIELD.equals(property)) {
                    super.addFilterValue(property, value == null ? null : value.toString(), params, filters);
                } else if (ShipmentConstants.SHIPPED_TO.equals(property)){
                    //create placeholder for 'in' operator
                    final List<String> in = new LinkedList<String>();
                    int num = 0;
                    for (final Object obj : ((List<?>) value)) {
                        final String key = defaultKey + "_" + num;
                        params.put(key, obj);
                        in.add(":" + key);
                        num++;
                    }

                    filters.add(TABLE + "." + SHIPPEDTO_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
                } else if (ShipmentConstants.SHIPPED_FROM.equals(property)){
                    //create placeholder for 'in' operator
                    final List<String> in = new LinkedList<String>();
                    int num = 0;
                    for (final Object obj : ((List<?>) value)) {
                        final String key = defaultKey + "_" + num;
                        params.put(key, obj);
                        in.add(":" + key);
                        num++;
                    }

                    filters.add(SHIPPEDFROM_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
                } else if (ShipmentConstants.SHIPPED_TO_DATE.equals(property)){
                    //shipped to date
                    params.put(defaultKey, value);
                    filters.add(TABLE + "." + SHIPMENTDATE_FIELD + " <= :" + defaultKey);
                } else if (ShipmentConstants.SHIPPED_FROM_DATE.equals(property)){
                    //shipped from date
                    params.put(defaultKey, value);
                    filters.add("COALESCE(" + TABLE + "." + LASTEVENT_FIELD + "," + TABLE + "." + SHIPMENTDATE_FIELD + ") >= :" + defaultKey);
                } else if (ShipmentConstants.SHIPMENT_DESCRIPTION.equals(property)){
                    params.put(defaultKey, "%" + value + "%");
                    filters.add(TABLE + "." + DESCRIPTION_FIELD + " like :" + defaultKey);
                } else if (ShipmentConstants.DEVICE_SN.equals(property)){
                    filters.add(ShipmentConstants.DEVICE_SN + " = :" + defaultKey);
                } else if (ShipmentConstants.GOODS.equals(property)){
                    params.put(defaultKey, "%" + value + "%");
                    final StringBuilder sb = new StringBuilder();
                    sb.append('(');
                    sb.append(TABLE + "." + DESCRIPTION_FIELD + " like :" + defaultKey);
                    sb.append(" or ");
                    sb.append(TABLE + "." + PALETTID_FIELD + " like :" + defaultKey);
                    sb.append(" or ");
                    sb.append(TABLE + "." + ASSETNUM_FIELD + " like :" + defaultKey);
                    sb.append(')');
                    filters.add(sb.toString());
                } else if (ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS.equals(property)){
                    //nothing in where clause
                } else {
                    super.addFilterValue(property, value, params, filters);
                }
            }
        };
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#createNewFrom(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Shipment createNewFrom(final ShipmentTemplate tpl) {
        if (tpl == null || tpl.getId() == null) {
            return null;
        }

        //in fact the template and the shipment is some table.
        final SelectAllSupport support = createSelectAllSupport();
        support.addAliases(getPropertyToDbMap());

        final Filter f = new Filter();
        f.addFilter(ID_FIELD, tpl.getId());

        support.buildSelectAll(f, null, null);

        final List<Shipment> list = findAll(support);
        if (!list.isEmpty()) {
            final Shipment s = list.get(0);
            s.setId(null);
            return s;
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#updateEta(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    public void updateEta(final Shipment s, final Date eta) {
        if (s == null) {
            return;
        }
        s.setEta(eta);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", s.getId());
        params.put("eta", eta);

        jdbc.update("update " + TABLE + " set " + ETA_FIELD + " = :eta where " + ID_FIELD + "=:s",
                params);
        shipmentBeanDao.clearShipmentBean(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#updateSiblingInfo(com.visfresh.entities.Shipment)
     */
    @Override
    public void updateSiblingInfo(final Shipment s) {
        s.setSiblingCount(s.getSiblings().size());

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", s.getId());
        params.put("siblings", s.getSiblingCount() == 0 ? null : StringUtils.combine(s.getSiblings(), ","));
        params.put("siblingCount", s.getSiblingCount());

        jdbc.update("update " + TABLE
                + " set " + SIBLINGCOUNT_FIELD + " = :siblingCount"
                + "," + SIBLINGS_FIELD + " = :siblings"
                + " where " + ID_FIELD + "=:s",
                params);

        shipmentBeanDao.clearShipmentBean(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#updateLastEventDate(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    public void updateLastEventDate(final Shipment s, final Date lev) {
        if (s == null) {
            return;
        }
        s.setEta(lev);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", s.getId());
        params.put("lev", lev);

        jdbc.update("update " + TABLE + " set " + LASTEVENT_FIELD + " = :lev where " + ID_FIELD + "=:s",
                params);
        shipmentBeanDao.clearShipmentBean(s.getId());
    }
    @Override
    public void markAsAutostarted(final Shipment s) {
        if (s == null) {
            return;
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", s.getId());

        jdbc.update("update " + TABLE + " set isautostart = true where " + ID_FIELD + "=:s",
                params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getTripCount(java.lang.Long)
     */
    @Override
    public Integer getTripCount(final Long shipmentId) {
        if (shipmentId == null) {
            return null;
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", shipmentId);

        final List<Map<String, Object>> rows = jdbc.queryForList("select s." + TRIPCOUNT_FIELD + " as tripcount from "
                + TABLE + " s where s." + ID_FIELD + "=:s", params);
        return rows.size() == 0 ? null : (((Number) rows.get(0).get("tripcount")).intValue());
    }
}
