/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.NotificationScheduleConstants;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationScheduleDaoImpl extends EntityWithCompanyDaoImplBase<NotificationSchedule, Long>
    implements NotificationScheduleDao {

    public static final String TABLE = "notificationschedules";

    private static final String ID_FIELD = "id";
    private static final String COMPANY_FIELD = "company";
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";

    private static final String PERSONAL_SCHEDULE_TABLE = "personalschedules";
    private static final String FIRSTNAME_FIELD = "firstname";
    private static final String LASTNAME_FIELD = "lastname";
    private static final String POSITION_FIELD = "position";
    private static final String SMS_FIELD = "sms";
    private static final String EMAIL_FIELD = "email";
    private static final String PUSHTOMOBILEAPP_FIELD = "pushtomobileapp";
    private static final String WEEKDAYS_FIELD = "weekdays";
    private static final String FROMTIME_FIELD = "fromtime";
    private static final String TOTIME_FIELD = "totime";
    private static final String SCHEDULE_FIELD = "schedule";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public NotificationScheduleDaoImpl() {
        super();
        propertyToDbFields.put(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME,
                NAME_FIELD);
        propertyToDbFields.put(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID,
                ID_FIELD);
        propertyToDbFields.put(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION,
                DESCRIPTION_FIELD);

    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends NotificationSchedule> S save(final S sched) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (sched.getId() == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD
                    , DESCRIPTION_FIELD
                    , COMPANY_FIELD
                ) + ")" + " values("
                    + ":"+ NAME_FIELD
                    + ", :" + DESCRIPTION_FIELD
                    + ", :" + COMPANY_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + NAME_FIELD
                + "," + DESCRIPTION_FIELD + "=:" + DESCRIPTION_FIELD
                + "," + COMPANY_FIELD + "=:" + COMPANY_FIELD
                + " where " + ID_FIELD + " = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, sched.getId());
        paramMap.put(NAME_FIELD, sched.getName());
        paramMap.put(DESCRIPTION_FIELD, sched.getDescription());
        paramMap.put(COMPANY_FIELD, sched.getCompany().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            sched.setId(keyHolder.getKey().longValue());
        }

        deletePersonalSchedulesFor(sched.getId());
        for (final PersonSchedule ps : sched.getSchedules()) {
            savePersonalSchedule(sched.getId(), ps);
        }
        return sched;
    }

    private void savePersonalSchedule(final Long schedId, final PersonSchedule ps) {
        final String sql = "insert into " + PERSONAL_SCHEDULE_TABLE + " (" + combine(
                COMPANY_FIELD
                , FIRSTNAME_FIELD
                , LASTNAME_FIELD
                , POSITION_FIELD
                , SMS_FIELD
                , EMAIL_FIELD
                , PUSHTOMOBILEAPP_FIELD
                , WEEKDAYS_FIELD
                , FROMTIME_FIELD
                , TOTIME_FIELD
                , SCHEDULE_FIELD
            ) + ")" + " values("
                + ":"+ COMPANY_FIELD
                + ", :" + FIRSTNAME_FIELD
                + ", :" + LASTNAME_FIELD
                + ", :" + POSITION_FIELD
                + ", :" + SMS_FIELD
                + ", :" + EMAIL_FIELD
                + ", :" + PUSHTOMOBILEAPP_FIELD
                + ", :" + WEEKDAYS_FIELD
                + ", :" + FROMTIME_FIELD
                + ", :" + TOTIME_FIELD
                + ", :" + SCHEDULE_FIELD
                + ")";

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(ID_FIELD, ps.getId());
        paramMap.put(COMPANY_FIELD, ps.getCompany());
        paramMap.put(FIRSTNAME_FIELD, ps.getFirstName());
        paramMap.put(LASTNAME_FIELD, ps.getLastName());
        paramMap.put(POSITION_FIELD, ps.getPosition());
        paramMap.put(SMS_FIELD, ps.getSmsNotification());
        paramMap.put(EMAIL_FIELD, ps.getEmailNotification());
        paramMap.put(PUSHTOMOBILEAPP_FIELD, ps.isPushToMobileApp());
        paramMap.put(WEEKDAYS_FIELD, convertToDatabaseColumn(ps.getWeekDays()));
        paramMap.put(FROMTIME_FIELD, ps.getFromTime());
        paramMap.put(TOTIME_FIELD, ps.getToTime());
        paramMap.put(SCHEDULE_FIELD, schedId);

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            ps.setId(keyHolder.getKey().longValue());
        }
    }
    /**
     * @param id
     * @return
     */
    private Collection<PersonSchedule> findPersonalSchedulesFor(final Long id) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(SCHEDULE_FIELD, id);
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from "
                + PERSONAL_SCHEDULE_TABLE
                + (id == null ? "" : " where " + SCHEDULE_FIELD + " = :" + SCHEDULE_FIELD),
                params);

        final List<PersonSchedule> result = new LinkedList<PersonSchedule>();
        for (final Map<String,Object> map : list) {
            final PersonSchedule ps = new PersonSchedule();
            ps.setId(((Number) map.get(ID_FIELD)).longValue());
            ps.setCompany((String) map.get(COMPANY_FIELD));
            ps.setFirstName((String) map.get(FIRSTNAME_FIELD));
            ps.setLastName((String) map.get(LASTNAME_FIELD));
            ps.setPosition((String) map.get(POSITION_FIELD));
            ps.setSmsNotification((String) map.get(SMS_FIELD));
            ps.setEmailNotification((String) map.get(EMAIL_FIELD));
            ps.setPushToMobileApp((Boolean) map.get(PUSHTOMOBILEAPP_FIELD));
            final boolean[] b = convertToEntityAttribute((String) map.get(WEEKDAYS_FIELD));
            System.arraycopy(b, 0, ps.getWeekDays(), 0, b.length);
            ps.setFromTime(((Number) map.get(FROMTIME_FIELD)).intValue());
            ps.setToTime(((Number) map.get(TOTIME_FIELD)).intValue());
            result.add(ps);
        }
        return result;
    }

    /**
     * @param id schedule ID.
     */
    private void deletePersonalSchedulesFor(final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        //delete personal schedule
        jdbc.update("delete from " + PERSONAL_SCHEDULE_TABLE + " where " + SCHEDULE_FIELD + " = :id",
                paramMap);
    }
    public String convertToDatabaseColumn(final boolean[] attribute) {
        final StringBuilder sb = new StringBuilder();
        for (final boolean b : attribute) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(b);
        }

        return sb.toString();
    }
    public boolean[] convertToEntityAttribute(final String dbData) {
        final boolean[] result = new boolean[7];
        final String[] split = dbData.split(", *");
        for (int i = 0; i < split.length; i++) {
            result[i] = Boolean.parseBoolean(split[i]);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
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
    protected NotificationSchedule createEntity(final Map<String, Object> map) {
        final NotificationSchedule no = new NotificationSchedule();

        no.setId(((Number) map.get(ID_FIELD)).longValue());
        no.setName((String) map.get(NAME_FIELD));
        no.setDescription((String) map.get(DESCRIPTION_FIELD));

        no.getSchedules().addAll(findPersonalSchedulesFor(no.getId()));
        return no;
    }
}
