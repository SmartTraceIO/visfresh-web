/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.SchedulePersonHowWhen;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleDaoTest
    extends BaseCrudTest<NotificationScheduleDao, NotificationSchedule, Long> {

    /**
     * Default constructor.
     */
    public NotificationScheduleDaoTest() {
        super(NotificationScheduleDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected NotificationSchedule createTestEntity() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setCompany(sharedCompany);
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        final SchedulePersonHowWhen ps = new SchedulePersonHowWhen();
        ps.setCompany("Any Company");
        ps.setEmailNotification("asuvoror");
        ps.setFirstName("First");
        ps.setFromTime(45);
        ps.setLastName("Last");
        ps.setPosition("Manager");
        ps.setPushToMobileApp(true);
        ps.setSmsNotification("11111111118");
        ps.setToTime(150);

        s.getSchedules().add(ps);
        return s;
    }
}
