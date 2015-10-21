/**
 *
 */
package com.visfresh.mpl.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.opengts.db.StatusCodes;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.EventData;
import org.opengts.dbtools.DBAdmin;
import org.opengts.dbtools.DBException;
import org.opengts.dbtools.DBFactory;
import org.opengts.dbtools.DBRecord;
import org.opengts.util.OrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultOpenJtsFacade implements OpenJtsFacade {
    private static final Logger log = LoggerFactory.getLogger(DefaultOpenJtsFacade.class);
    private final Map<String, Integer> statusCodes;

    /**
     * Default constructor.
     */
    public DefaultOpenJtsFacade() {
        super();

        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("AUT", StatusCodes.STATUS_LOCATION);
        map.put("BRT", StatusCodes.STATUS_LIGHTING_BRIGHTER);
        map.put("DRK", StatusCodes.STATUS_LIGHTING_DARKER);
        map.put("INIT", StatusCodes.STATUS_INITIALIZED);
        map.put("STP", StatusCodes.STATUS_ALARM_OFF);
        map.put("VIB", StatusCodes.STATUS_VIBRATION_ON);

        statusCodes = map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addUser(com.visfresh.entities.User)
     */
    @Override
    public void addUser(final User user, final String password) {
        final String accountId = createAccountId(user.getCompany());
        try {
            //create company account if need.
            Account acc = Account.getAccount(accountId);
            if (acc == null) {
                Account.createNewAccount(null, accountId, null);
                acc = Account.getAccount(accountId);
            }

            org.opengts.db.tables.User.createNewUser(acc, user.getLogin(), null, password);
        } catch (final DBException e) {
            log.error("Failed to create OpenGTS account " + accountId, e);
        }
    }

    /**
     * @param company
     * @return
     */
    protected static String createAccountId(final Company company) {
        return "visfresh-" + company.getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addDevice(com.visfresh.entities.Device)
     */
    @Override
    public void addDevice(final Device d) {
        try {
            final Account account = Account.getAccount(createAccountId(d.getCompany()));
            org.opengts.db.tables.Device device = org.opengts.db.tables.Device.getDevice(account, d.getId());
            if (device == null) {
                device = org.opengts.db.tables.Device.createNewDevice(account, d.getImei(), d.getId());
                log.debug("OpenGTS device has succesfully created for " + d.getId());
            }
        } catch (final DBException e) {
            log.error("Failed to add device " + d.getId(), e);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void addTrackerEvent(final TrackerEvent e) {
        final String accountID = createAccountId(e.getDevice().getCompany());
        try {
            final Account account = Account.getAccount(accountID);

            final String imei = e.getDevice().getImei();

            final org.opengts.db.tables.Device device = org.opengts.db.tables.Device.getDevice(
                    account, e.getDevice().getId());

            final EventData.Key evKey = new EventData.Key(accountID, imei,
                    e.getTime().getTime() / 1000l, statusCodes.get(e.getType()));
            final EventData evdb = evKey.getDBRecord();
            evdb.setLatitude(e.getLatitude());
            evdb.setLongitude(e.getLongitude());
            evdb.setBatteryLevel(e.getBattery());
            evdb.setBatteryTemp(e.getTemperature());

            if (device.insertEventData(evdb)) {
                log.debug("Device message has saved for " + imei);
            } else {
                // -- this will display an error if it was unable to store the event
                log.error("Failed to save event data for device " + imei);
            }
        } catch (final DBException exc) {
            log.error("Failed to add OpenGTS event", exc);
        }
    }

    @PostConstruct
    public void initialize() {
        @SuppressWarnings("rawtypes")
        final OrderedMap<String, DBFactory<? extends DBRecord>> factories = DBAdmin.getTableFactoryMap();

        final String key = factories.getFirstKey();
        try {
            if (!factories.get(key).tableExists()) {
                final int size = factories.size();
                for (int i = 0; i < size; i++) {
                    factories.get(i).createTable();
                }
            }
        } catch (final DBException e) {
            log.error("Failed to initialize OpenGTS data base");
        }
    }
}
