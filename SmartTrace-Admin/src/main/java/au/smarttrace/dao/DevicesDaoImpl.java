/**
 *
 */
package au.smarttrace.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.smarttrace.Color;
import au.smarttrace.Device;
import au.smarttrace.DeviceModel;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.device.DevicesDao;
import au.smarttrace.device.GetDevicesRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DevicesDaoImpl extends AbstractDao implements DevicesDao {
    private static final Logger log = LoggerFactory.getLogger(DevicesDaoImpl.class);
    private final Map<String, String> fieldAliases = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public DevicesDaoImpl() {
        super();
        fieldAliases.put("description", "description");
        fieldAliases.put("name", "name");
        fieldAliases.put("imei", "imei");
        fieldAliases.put("active", "active");
        fieldAliases.put("company", "company");
        fieldAliases.put("model", "model");
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#getByImei(java.lang.String)
     */
    @Override
    public Device getByImei(final String imei) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", imei);

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from devices where imei = :imei", params);
        if (rows.size() > 0) {
            return createDeviceFromDbRow(rows.get(0));
        }
        return null;
    }

    /**
     * @param row DB row.
     * @return
     */
    private Device createDeviceFromDbRow(final Map<String, Object> row) {
        final Device d = new Device();
        d.setName((String) row.get("name"));
        d.setDescription((String) row.get("description"));
        d.setImei((String) row.get("imei"));
        d.setModel(DeviceModel.valueOf((String) row.get("model")));
        d.setActive(Boolean.TRUE.equals(row.get("active")));
        d.setColor(parseColor((String) row.get("color")));
        d.setCompany(((Number) row.get("company")).longValue());
        return d;
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#deleteDevice(java.lang.String)
     */
    @Override
    public void deleteDevice(final String imei) {
        final Map<String, Object> params = new HashMap<>();
        params.put("device", imei);

        //simulators
        jdbc.update("delete from simulators where source = :device", params);
        //device commands
        jdbc.update("delete from devicecommands where device = :device", params);
        //groups to new device
        jdbc.update("delete from devicegrouprelations where device = :device", params);
        //move arrivals to backup
        jdbc.update("delete from arrivals where device = :device", params);
        //alerts
        jdbc.update("delete from alerts where device = :device", params);
        //readings
        jdbc.update("delete from trackerevents where device = :device", params);
        //shipments
        jdbc.update("delete from shipments where device = :device", params);
        //device states
        jdbc.update("delete from devicestates where device = :device", params);
        //delete device
        jdbc.update("delete from devices where imei = :device", params);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#saveDevice(au.smarttrace.Device)
     */
    @Override
    public void createDevice(final Device device) {
        final Map<String, Object> params = toDbFieldsIgnoreImei(device);
        params.put("imei", device.getImei());

        final Set<String> fields = new HashSet<>(params.keySet());
        final String sql = "insert into devices(" + String.join(",", fields) + ")"
                + " values(:" + String.join(",:", fields) + ")";
        jdbc.update(sql, params);
    }

    /**
     * @param device
     * @return
     */
    protected Map<String, Object> toDbFieldsIgnoreImei(final Device device) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("name", device.getName());
        paramMap.put("model", device.getModel().name());
        paramMap.put("description", device.getDescription());
        paramMap.put("company", device.getCompany());
        paramMap.put("active", device.isActive());
        paramMap.put("color", device.getColor() == null ? null : device.getColor().name());
        return paramMap;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#saveDevice(au.smarttrace.Device)
     */
    @Override
    public void updateDevice(final Device device) {
        final Map<String, Object> params = toDbFieldsIgnoreImei(device);

        final List<String> sets = new LinkedList<>();
        for (final String key : params.keySet()) {
            sets.add(key + " = :" + key);
        }
        final String sql = "update devices set " + String.join(", ", sets) + " where imei = :imei";

        params.put("imei", device.getImei());
        jdbc.update(sql, params);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#getTripCount(au.smarttrace.Device)
     */
    @Override
    public int getTripCount(final Device device) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", device.getImei());

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select tripcount from devices where imei = :imei", params);
        if (rows.size() > 0) {
            return ((Number) rows.get(0).get("tripcount")).intValue();
        }

        return 0;
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#setTripCount(au.smarttrace.Device, int)
     */
    @Override
    public void setTripCount(final Device device, final int tripCount) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", device.getImei());
        params.put("trip", tripCount);

        jdbc.update("update devices set tripcount = :trip where imei = :imei", params);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#moveToNewCompany(au.smarttrace.Device, au.smarttrace.Company, au.smarttrace.Device)
     */
    @Override
    public void moveToNewCompany(final Device device, final Long company, final Device backup) {
        final Map<String, Object> params = new HashMap<>();
        params.put("device", device.getImei());
        params.put("backup", backup.getImei());
        params.put("company", company);

        //simulators
        jdbc.update("delete from simulators where source = :device", params);
        //device commands
        jdbc.update("delete from devicecommands where device = :device", params);
        //move device to new company
        jdbc.update("update devices set company = :company where imei = :device", params);
        //groups to new device
        jdbc.update("update devicegrouprelations set device = :backup where device = :device", params);
        //move arrivals to backup
        jdbc.update("update arrivals set device = :backup where device = :device", params);
        //alerts
        jdbc.update("update alerts set device = :backup where device = :device", params);
        //readings
        jdbc.update("update trackerevents set device = :backup where device = :device", params);
        //shipments
        jdbc.update("update shipments set device = :backup where device = :device", params);
        //device states
        jdbc.update("delete from devicestates where device = :device", params);
        //close all active shipments
        jdbc.update("update shipments set status = 'Ended' where status <> 'Ended'"
                + " and  status <> 'Arrived' and not istemplate and device = :backup", params);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesDao#getDevices(au.smarttrace.device.GetDevicesRequest)
     */
    @Override
    public ListResponse<Device> getDevices(final GetDevicesRequest req) {
        final Map<String, Object> params = new HashMap<>();

        final List<String> where = new LinkedList<>();
        //companies filter.
        if (req.getCompanyFilter().size() > 0) {
            where.add("company in (" + String.join(",",
                    GetListQueryUtils.toStringList(req.getCompanyFilter())) + ")");
        }

        //name filter
        if (req.getNameFilter() != null) {
            final List<String> or = new LinkedList<>();
            final String[] tokens = GetListQueryUtils.tokenize(req.getNameFilter());
            for (int i = 0; i < tokens.length; i++) {
                final String t = tokens[i];
                final String key = "name_" + i;
                or.add("name like :" + key);

                params.put(key, "%" + t + "%");
            }

            if (or.size() > 0) {
                where.add("(" + String.join(" or ", or) + ")");
            }
        }

        //IMEI filter
        if (req.getImeiFilter() != null) {
            final List<String> or = new LinkedList<>();
            final String[] tokens = GetListQueryUtils.tokenize(req.getImeiFilter());
            for (int i = 0; i < tokens.length; i++) {
                final String t = tokens[i];
                final String key = "imei_" + i;
                or.add("imei like :" + key);

                params.put(key, "%" + t + "%");
            }

            if (or.size() > 0) {
                where.add("(" + String.join(" or ", or) + ")");
            }
        }

        //create query
        StringBuilder sql = new StringBuilder("select * from devices");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        //add ordering
        final List<String> orders = new LinkedList<>();
        for (final Order order : req.getOrders()) {
            final String f = fieldAliases.get(order.getField());
            //if alias exists add field to order
            if (f != null) {
                orders.add(f + (order.isAscent() ? "" : " desc"));
            }
        }

        //if order list empty, add default ordering
        if(orders.size() == 0) {
            orders.add("imei");
        }
        sql.append(" order by ");
        sql.append(String.join(",", orders));

        //add limitation
        sql.append(" limit " + (req.getPage() * req.getPageSize()) + "," + req.getPageSize());

        //request data
        final ListResponse<Device> resp = new ListResponse<>();
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);
        for (final Map<String, Object> row : rows) {
            resp.getItems().add(createDeviceFromDbRow(row));
        }

        //add total count
        sql = new StringBuilder("select count(*) as totalCount from devices");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        rows = jdbc.queryForList(sql.toString(), params);
        if (rows.size() > 0) {
            resp.setTotalCount(((Number) rows.get(0).get("totalCount")).intValue());
        }

        return resp;
    }
    /**
     * @param str
     * @return
     */
    private Color parseColor(final String str) {
        if (str == null) {
            return null;
        }

        try {
            return Color.valueOf(str);
        } catch (final Exception e) {
            log.error("Failed to parse color value '" + str + "'", e);
            return null;
        }
    }
}
