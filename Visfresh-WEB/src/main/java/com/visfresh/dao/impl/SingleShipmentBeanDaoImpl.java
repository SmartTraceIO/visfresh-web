/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.SingleShipmentBeanDao;
import com.visfresh.entities.Device;
import com.visfresh.io.json.SingleShipmentBeanSerializer;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentBeanDaoImpl implements SingleShipmentBeanDao {
    private static final String TABLE = "singleshipments";
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public SingleShipmentBeanDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SingleShipmentBeanDao#saveShipmentBean(com.visfresh.io.shipment.SingleShipmentBean)
     */
    @Override
    public void saveShipmentBean(final SingleShipmentBean bean) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("shipment", bean.getShipmentId());
        paramMap.put("sn", Device.getSerialNumber(bean.getDevice()));
        paramMap.put("trip", bean.getTripCount());
        paramMap.put("bean", serialize(bean));

        final StringBuilder sql = new StringBuilder("insert into " + TABLE
                + "(shipment, sn, trip, bean) values (:shipment, :sn, :trip, :bean)"
                + " ON DUPLICATE KEY UPDATE bean = :bean");
        jdbc.update(sql.toString(), paramMap);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SingleShipmentBeanDao#getShipmentBeanIncludeSiblings(java.lang.Long)
     */
    @Override
    public List<SingleShipmentBean> getShipmentBeanIncludeSiblings(final Long shipmentId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipmentId);
        return getShipmentBeanIncludeSiblings("s.id = :shipment", params);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SingleShipmentBeanDao#getShipmentBeanIncludeSiblings(java.lang.String, java.lang.Integer)
     */
    @Override
    public List<SingleShipmentBean> getShipmentBeanIncludeSiblings(final String sn, final Integer tripCount) {
        final Map<String, Object> params = new HashMap<>();
        params.put("sn", sn);
        params.put("trip", tripCount);
        return getShipmentBeanIncludeSiblings(
                "s.device like concat('%', :sn, '_') and s.tripcount = :trip",
                params);
    }
    private List<SingleShipmentBean> getShipmentBeanIncludeSiblings(
            final String where, final Map<String, Object> params) {
        final String sql = "select ss.bean as bean from shipments s"
            + " join singleshipments ss on ss.shipment = s.id"
            + " where "
            + where
            + " union"
            + " select ss.bean as bean from shipments s"
            + " join shipments sib on"
            + " sib.siblings = s.id or sib.siblings like concat(s.id, ',%')"
            + " or sib.siblings like concat('%,', s.id, ',%') or sib.siblings like concat('%,', s.id)"
            + " join singleshipments ss on ss.shipment = sib.id where "
            + where;

        final SingleShipmentBeanSerializer ser = new SingleShipmentBeanSerializer();
        final List<SingleShipmentBean> beans = new LinkedList<>();
        for (final Map<String, Object> rows : jdbc.queryForList(sql, params)) {
            final SingleShipmentBean bean = ser.parseSingleShipmentBean(
                    SerializerUtils.parseJson((String) rows.get("bean")));
            beans.add(bean);
        }
        return beans;
    }
    /**
     * @param bean single shipment bean.
     * @return
     */
    private String serialize(final SingleShipmentBean bean) {
        return new SingleShipmentBeanSerializer().toJson(bean).toString();
    }
}
