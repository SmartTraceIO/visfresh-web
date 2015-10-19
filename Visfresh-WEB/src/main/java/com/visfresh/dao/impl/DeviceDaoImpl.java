/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceDaoImpl extends DaoImplBase<Device, String> implements DeviceDao {
    /**
     * Table name.
     */
    public static final String TABLE = "devices";
    /**
     * Description field.
     */
    private static final String DESCRIPTION_FIELD = "description";
    /**
     * Name field.
     */
    private static final String NAME_FIELD = "name";
    /**
     * ID field.
     */
    protected static final String ID_FIELD = "id";
    /**
     * Name field.
     */
    private static final String IMEI_FIELD = "imei";
    /**
     * Serial number field.
     */
    private static final String SN_FIELD = "sn";
    /**
     * Company name.
     */
    protected static final String COMPANY_FIELD = "company";

    /**
     * Default constructor.
     */
    public DeviceDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Device> S save(final S device) {
        final String namePlaceHolder = "name";
        final String descriptionPlaceHolder = "description";
        final String imeiPlaceHolder = "imei";
        final String snPlaceHolder = "sn";
        final String companyPlaceHolder = "company";
        final String idPlaceHolder = "id";
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (findOne(device.getId()) == null) {
            //insert
            paramMap.put("id", device.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD,
                    ID_FIELD,
                    IMEI_FIELD,
                    SN_FIELD,
                    COMPANY_FIELD,
                    DESCRIPTION_FIELD
                ) + ")" + " values("
                    + ":"+ namePlaceHolder
                    + ", :" + idPlaceHolder
                    + ", :" + imeiPlaceHolder
                    + ", :" + snPlaceHolder
                    + ", :" + companyPlaceHolder
                    + ", :" + descriptionPlaceHolder
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + namePlaceHolder + ","
                + ID_FIELD + "=:" + idPlaceHolder + ","
                + IMEI_FIELD + "=:" + imeiPlaceHolder + ","
                + SN_FIELD + "=:" + snPlaceHolder + ","
                + COMPANY_FIELD + "=:" + companyPlaceHolder + ","
                + DESCRIPTION_FIELD + "=:" + descriptionPlaceHolder
                + " where id = :" + ID_FIELD
            ;
        }

        paramMap.put(idPlaceHolder, device.getId());
        paramMap.put(namePlaceHolder, device.getName());
        paramMap.put(descriptionPlaceHolder, device.getDescription());
        paramMap.put(imeiPlaceHolder, device.getImei());
        paramMap.put(snPlaceHolder, device.getSn());
        paramMap.put(companyPlaceHolder, device.getCompany().getId());

        jdbc.update(sql, paramMap);

        return device;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public Device findOne(final String id) {
        final String entityName = "d";
        final String companyEntityName = "c";
        final String resultPrefix = "d_";
        final String companyResultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(id, entityName,
                companyEntityName, resultPrefix, companyResultPrefix);

        return list.size() == 0 ? null : createDevice(resultPrefix, companyResultPrefix, list.get(0));
    }

    /**
     * @param id
     * @param entityName
     * @param companyEntityName
     * @param resultPrefix
     * @param companyResultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelect(final String id,
            final String entityName, final String companyEntityName,
            final String resultPrefix, final String companyResultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        final Map<String, String> companyFields = CompanyDaoImpl.createSelectAsMapping(
                companyEntityName, companyResultPrefix);
        params.putAll(fields);
        params.putAll(companyFields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + ", " + buildSelectAs(companyFields)
                + " from "
                + TABLE + " " + entityName
                + ", " + CompanyDaoImpl.TABLE + " " + companyEntityName
                + " where "
                + entityName + "." + COMPANY_FIELD + " = " + companyEntityName + "." + CompanyDaoImpl.ID_FIELD
                + (id == null ? "" : " and " + entityName + "." + ID_FIELD + " = :id"),
                params);
        return list;
    }

    /**
     * @param resultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    public static Device createDevice(final String resultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        final Device d = new Device();
        d.setCompany(CompanyDaoImpl.createCompany(companyResultPrefix, map));
        d.setId((String) map.get(resultPrefix + ID_FIELD));
        d.setName((String) map.get(resultPrefix + NAME_FIELD));
        d.setDescription((String) map.get(resultPrefix + DESCRIPTION_FIELD));
        d.setSn((String) map.get(resultPrefix + SN_FIELD));
        d.setImei((String) map.get(resultPrefix + IMEI_FIELD));
        return d;
    }

    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    public static Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + ID_FIELD, resultPrefix + ID_FIELD);
        map.put(entityName + "." + NAME_FIELD, resultPrefix + NAME_FIELD);
        map.put(entityName + "." + DESCRIPTION_FIELD, resultPrefix + DESCRIPTION_FIELD);
        map.put(entityName + "." + IMEI_FIELD, resultPrefix + IMEI_FIELD);
        map.put(entityName + "." + SN_FIELD, resultPrefix + SN_FIELD);
        return map ;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<Device> findAll() {
        final String entityName = "d";
        final String companyEntityName = "c";
        final String resultPrefix = "d_";
        final String companyResultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(null,
                entityName, companyEntityName, resultPrefix, companyResultPrefix);

        final List<Device> result = new LinkedList<Device>();
        for (final Map<String,Object> map : list) {
            result.add(createDevice(resultPrefix, companyResultPrefix, map));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final String id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }
}
