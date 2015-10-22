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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.impl.ShipmentDeviceInfo;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
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
    @Autowired
    private ShipmentDao shipmentDao;

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
        try {
            //create company account if need.
            final Account acc = createAccountIfNeed(user.getCompany());
            org.opengts.db.tables.User.createNewUser(acc, user.getLogin(), null, password);
            log.debug("OpenGTS user " + user.getLogin() + " has autocreated");
        } catch (final DBException e) {
            log.error("Failed to create OpenGTS account " + createAccountId(user.getCompany()), e);
        }
    }
    /**
     * @param company
     * @return
     */
    public static String createAccountId(final Company company) {
        return "visfresh-" + company.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void addTrackerEvent(final Shipment shipment, final TrackerEvent e) {
        try {
            final Account a = createAccountIfNeed(e.getDevice().getCompany());
            final org.opengts.db.tables.Device device = createDeviceIfNeed(a, shipment, e.getDevice());

            //create event.
            final EventData.Key evKey = new EventData.Key(a.getAccountID(), device.getDeviceID(),
                    e.getTime().getTime() / 1000l, statusCodes.get(e.getType()));
            final EventData evdb = evKey.getDBRecord();
            evdb.setLatitude(e.getLatitude());
            evdb.setLongitude(e.getLongitude());
            evdb.setBatteryLevel(e.getBattery());
            evdb.setBatteryTemp(e.getTemperature());

            if (device.insertEventData(evdb)) {
                log.debug("Device message has saved for " + device.getDeviceID());
            } else {
                // -- this will display an error if it was unable to store the event
                log.error("Failed to save event data for device " + device.getDeviceID());
            }
        } catch (final DBException exc) {
            log.error("Failed to add OpenGTS event", exc);
        }
    }
    /**
     * @param shipment
     * @param d
     * @return
     * @throws DBException
     */
    private org.opengts.db.tables.Device createDeviceIfNeed(final Account a, final Shipment shipment,
            final Device d) throws DBException {
        final ShipmentDeviceInfo info = shipmentDao.getShipmentDeviceInfo(shipment, d);
        final String deviceId = d.getId() + "." + info.getTripCount();

        org.opengts.db.tables.Device device = org.opengts.db.tables.Device.getDevice(a, deviceId);
        if (device == null) {
            device = org.opengts.db.tables.Device.getDevice(a, deviceId);
            device.setDescription(d.getName());
            device.save();
            log.debug("OpenGTS device " + deviceId
                    + " has autocreated for device " + d.getName());
        }
        return device;
    }
    /**
     * @param company
     * @return
     * @throws DBException
     */
    private Account createAccountIfNeed(final Company company) throws DBException {
        final String accountId = createAccountId(company);
        Account acc = Account.getAccount(accountId);
        if (acc == null) {
            acc = Account.createNewAccount(null, accountId, null);
            log.debug("OpenGTS account " + accountId + " has autocreated for " + company.getName());
        }
        return acc;
    }
    @PostConstruct
    public void initialize() {
        try {
            @SuppressWarnings("rawtypes")
            final OrderedMap<String, DBFactory<? extends DBRecord>> factories = DBAdmin.getTableFactoryMap();

            final String key = factories.getFirstKey();
            if (!factories.get(key).tableExists()) {
                final int size = factories.size();
                for (int i = 0; i < size; i++) {
                    factories.get(i).createTable();
                }
            }
        } catch (final Exception e) {
            log.error("Failed to initialize OpenGTS data base", e);
        }
    }
}
