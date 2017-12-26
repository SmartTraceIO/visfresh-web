/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.CompanyConstants;
import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.PaymentMethod;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CompanyDaoImpl extends DaoImplBase<Company, Company, Long> implements CompanyDao {
    public static final String TABLE = "companies";
    /**
     * Description field.
     */
    private static final String DESCRIPTION_FIELD = "description";
    private static final String NAME_FIELD = "name";
    public static final String ID_FIELD = "id";

    protected static String ADDRESS_FIELD = "address";
    protected static String CONTACT_PERSON_FIELD = "contactperson";
    protected static String EMAIL_FIELD = "email";
    protected static String TIME_ZONE_FIELD = "timezone";
    protected static String START_DATE_FIELD = "startdate";
    protected static String TRACKERS_EMAIL_FIELD = "trackersemail";
    protected static String PAYMENT_METHOD_FIELD = "paymentmethod";
    protected static String BILLING_PERSON_FIELD = "billingperson";
    protected static String LANGUAGE_FIELD = "language";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public CompanyDaoImpl() {
        super();
        propertyToDbFields.put(CompanyConstants.ID, ID_FIELD);
        propertyToDbFields.put(CompanyConstants.NAME, NAME_FIELD);
        propertyToDbFields.put(CompanyConstants.DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(CompanyConstants.ADDRESS, ADDRESS_FIELD);
        propertyToDbFields.put(CompanyConstants.CONTACT_PERSON, CONTACT_PERSON_FIELD);
        propertyToDbFields.put(CompanyConstants.EMAIL, EMAIL_FIELD);
        propertyToDbFields.put(CompanyConstants.TIME_ZONE, TIME_ZONE_FIELD);
        propertyToDbFields.put(CompanyConstants.START_DATE, START_DATE_FIELD);
        propertyToDbFields.put(CompanyConstants.TRACKERS_EMAIL, TRACKERS_EMAIL_FIELD);
        propertyToDbFields.put(CompanyConstants.PAYMENT_METHOD, PAYMENT_METHOD_FIELD);
        propertyToDbFields.put(CompanyConstants.BILLING_PERSON, BILLING_PERSON_FIELD);
        propertyToDbFields.put(CompanyConstants.LANGUAGE, LANGUAGE_FIELD);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Company> S save(final S company) {
        final Map<String, Object> paramMap = createParameterMap(company);
        final LinkedList<String> fields = new LinkedList<String>(paramMap.keySet());

        String sql;
        if (company.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            company.setId(keyHolder.getKey().longValue());
        }

        return company;
    }
    private Map<String, Object> createParameterMap(final Company c) {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ID_FIELD, c.getId());
        paramMap.put(NAME_FIELD, c.getName());
        paramMap.put(DESCRIPTION_FIELD, c.getDescription());

        paramMap.put(ADDRESS_FIELD, c.getAddress());
        paramMap.put(CONTACT_PERSON_FIELD, c.getContactPerson());
        paramMap.put(EMAIL_FIELD, c.getEmail());
        paramMap.put(TIME_ZONE_FIELD, c.getTimeZone() == null ? null : c.getTimeZone().getID());
        paramMap.put(START_DATE_FIELD, c.getStartDate());
        paramMap.put(TRACKERS_EMAIL_FIELD, c.getTrackersEmail());
        paramMap.put(PAYMENT_METHOD_FIELD, c.getPaymentMethod() == null ? null : c.getPaymentMethod().name());
        paramMap.put(BILLING_PERSON_FIELD, c.getBillingPerson());
        paramMap.put(LANGUAGE_FIELD, c.getLanguage() == null ? null : c.getLanguage().name());
        return paramMap;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Company createEntity(final Map<String, Object> map) {
        final Company c = new Company();
        c.setId(((Number) map.get(ID_FIELD)).longValue());
        c.setName((String) map.get(NAME_FIELD));
        c.setDescription((String) map.get(DESCRIPTION_FIELD));

        c.setAddress((String) map.get(ADDRESS_FIELD));
        c.setContactPerson((String) map.get(CONTACT_PERSON_FIELD));
        c.setEmail((String) map.get(EMAIL_FIELD));

        final String tz = (String) map.get(TIME_ZONE_FIELD);
        if (tz != null) {
            c.setTimeZone(TimeZone.getTimeZone(tz));
        }

        c.setStartDate((Date) map.get(START_DATE_FIELD));
        c.setTrackersEmail((String) map.get(TRACKERS_EMAIL_FIELD));

        final String pm = (String) map.get(PAYMENT_METHOD_FIELD);
        if (pm != null) {
            c.setPaymentMethod(PaymentMethod.valueOf(pm));
        }

        c.setBillingPerson((String) map.get(BILLING_PERSON_FIELD));

        final String lang = (String) map.get(LANGUAGE_FIELD);
        if (lang != null) {
            c.setLanguage(Language.valueOf(lang));
        }

        return c;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Company t, final Map<String, Object> map,
            final Map<String, Object> cache) {
        // nothing to resolve
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
}
