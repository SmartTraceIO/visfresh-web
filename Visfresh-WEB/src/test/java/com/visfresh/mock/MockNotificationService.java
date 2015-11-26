/**
 *
 */
package com.visfresh.mock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.services.NotificationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockNotificationService implements NotificationService {
    private List<MockNotificationServiceListener> listeners
        = new CopyOnWriteArrayList<MockNotificationServiceListener>();

    /**
     * Default constructor.
     */
    public MockNotificationService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.NotificationService#sendNotification(com.visfresh.entities.PersonSchedule, com.visfresh.entities.NotificationIssue)
     */
    @Override
    public void sendNotification(final PersonSchedule s, final NotificationIssue issue) {
        //nothing do, only notify listeners.
        for (final MockNotificationServiceListener l : listeners) {
            l.sendingNotification(s, issue);
        }
    }
    /**
     * @param l mock notification service listener.
     */
    public void addMockNotificationServiceListener(final MockNotificationServiceListener l) {
        listeners.add(l);
    }
    /**
     * @param l mock notification service listener.
     */
    public void removeMockNotificationServiceListener(final MockNotificationServiceListener l) {
        listeners.remove(l);
    }
}
