/**
 *
 */
package com.visfresh.dao.impl.lite;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;

/**
 * Warning!!! The readings should be sorted by time and ID before process them
 * by given builder.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyLocationsBuilder {
    protected final Map<Long, List<List<LiteKeyLocation>>> groups = new HashMap<>();
    private ShipmentSwitchListener listener;

    private boolean currentIsInUpperHotLimit = false;
    private boolean currentIsInLowerColdLimit = false;
    private LiteShipment currentShipment;
    private Long currentId;
    private List<LiteKeyLocation> currentGroup;
    private int numberOfKeyLocations = 20;

    /**
     * Default constructor.
     */
    public KeyLocationsBuilder() {
        super();
    }

    /**
     * @param s given shipment.
     * @param readingId reading ID.
     * @param time reading time.
     * @param temperature reading temperature.
     * @param hasAlerts whether or not the reading has alerts.
     */
    public void addNextReading(final LiteShipment s, final long readingId, final Date time,
            final double temperature,
            final boolean hasAlerts) {
        //ignore if reading by given ID already processed
        if (new Long(readingId).equals(currentId)) {
            return;
        }

        final boolean isInUpperHot = s.getUpperTemperatureLimit() <= temperature;
        final boolean isInLowerCold = s.getLowerTemperatureLimit() >= temperature;
        final boolean firstEvent = groups.get(s.getShipmentId()) == null;

        final boolean shipmentSwitched = currentShipment != null
                && !currentShipment.getShipmentId().equals(s.getShipmentId());

        //check should open new group for current reading
        final boolean shouldStartNewGroup =
                currentGroup == null ||
                hasAlerts ||
                shipmentSwitched ||
                isInLowerCold != currentIsInLowerColdLimit ||
                isInUpperHot != currentIsInUpperHotLimit;

        //get or create groups container
        List<List<LiteKeyLocation>> shipmentGroups = this.groups.get(s.getShipmentId());
        if (shipmentGroups == null) {
            shipmentGroups = new LinkedList<>();
            this.groups.put(s.getShipmentId(), shipmentGroups);
        }

        //add current group to shipment groups if need
        if (shouldStartNewGroup) {
            currentGroup = new LinkedList<>();
            shipmentGroups.add(currentGroup);

            if (shipmentSwitched && listener != null) {
                listener.shipmentSwitched(currentShipment.getShipmentId(), s.getShipmentId());
            }
        }

        //create and add key location
        currentGroup.add(createKeyLocation(readingId, time, temperature));

        //check possible should close given group
        final boolean shouldCloseGivenGroup = firstEvent || hasAlerts;
        if (shouldCloseGivenGroup) {
            currentGroup = null;
        }

        //save current state
        currentIsInLowerColdLimit = isInLowerCold;
        currentIsInUpperHotLimit = isInUpperHot;
        currentId = readingId;
        currentShipment = s;
    }
    /**
     * @param readingId
     * @param time
     * @param temperature
     * @return
     */
    private LiteKeyLocation createKeyLocation(final long readingId, final Date time, final double temperature) {
        final LiteKeyLocation loc = new LiteKeyLocation();
        loc.setId(readingId);
        loc.setTemperature(temperature);
        loc.setTime(time);
        return loc;
    }

    public Map<Long, List<LiteKeyLocation>> build() {
        //create groups copy
        final Map<Long, List<List<LiteKeyLocation>>> groupsCopy = new HashMap<>();
        for (final Map.Entry<Long, List<List<LiteKeyLocation>>> e : groups.entrySet()) {
            groupsCopy.put(e.getKey(), new LinkedList<>(e.getValue()));
        }

        for (final Long shipmentId : groupsCopy.keySet()) {
            //update latest group for each shipment
            final List<List<LiteKeyLocation>> shipmentGroups = groupsCopy.get(shipmentId);
            singleGroupForLastReading(shipmentGroups);

            //next splitting of groups for support of number of key locations
            groupsCopy.put(shipmentId, finalSplit(shipmentGroups));
        }

        return buildKeyLocations(groupsCopy);
    }

    /**
     * @param groups
     * @return finally split groups.
     */
    protected List<List<LiteKeyLocation>> finalSplit(final List<List<LiteKeyLocation>> groups) {
        //calculate total number of readings
        final int totalReadings = groups.stream().mapToInt(l -> l.size()).sum();
        int numPrcessed = 0;

        final List<List<LiteKeyLocation>> finished = new LinkedList<>();
        for (final List<LiteKeyLocation> list : groups) {
            int needGroups = getNumberOfKeyLocations() - finished.size();
            if (needGroups <= 0) {
                //then copy from origin group list
                finished.add(list);
                numPrcessed += list.size();
            } else {
                //need splitting
                int readingsPerGroup = calculateReadingsInGroup(totalReadings, numPrcessed, needGroups);
                final List<LiteKeyLocation> reminder = new LinkedList<>(list);

                while (reminder.size() >= readingsPerGroup) {
                    needGroups = getNumberOfKeyLocations() - finished.size();
                    if (needGroups <= 0) {
                        break;
                    }
                    readingsPerGroup = calculateReadingsInGroup(totalReadings, numPrcessed, needGroups);

                    //move items from reminder to new group
                    final List<LiteKeyLocation> newGroup = new LinkedList<>();
                    for (int i = 0; i < readingsPerGroup; i++) {
                        newGroup.add(reminder.remove(0));
                    }
                    finished.add(newGroup);

                    //update number of processed items
                    numPrcessed += readingsPerGroup;
                }

                if (!reminder.isEmpty()) {
                    finished.add(reminder);
                }
            }
        }

        return finished;
    }

    /**
     * @param totalReadings
     * @param numPrcessed
     * @param needGroups
     * @return
     */
    protected int calculateReadingsInGroup(final int totalReadings, final int numPrcessed, final int needGroups) {
        return Math.max(1, (int) Math.round((totalReadings - numPrcessed) / (double) needGroups));
    }

    /**
     * @param shipmentGroups
     */
    private void singleGroupForLastReading(final List<List<LiteKeyLocation>> shipmentGroups) {
        final List<LiteKeyLocation> latestGroup = shipmentGroups.get(shipmentGroups.size() - 1);
        if (latestGroup.size() > 1) {
            //create separate group for last reading
            final List<LiteKeyLocation> lastReadingGroup = new LinkedList<>();
            lastReadingGroup.add(latestGroup.remove(latestGroup.size() - 1));

            //add last reading group to shipment groups as final group
            shipmentGroups.add(lastReadingGroup);
        }
    }
    /**
     * @return the numberOfKeyLocations
     */
    public int getNumberOfKeyLocations() {
        return numberOfKeyLocations;
    }
    /**
     * @param numberOfKeyLocations the numberOfKeyLocations to set
     */
    public void setNumberOfKeyLocations(final int numberOfKeyLocations) {
        this.numberOfKeyLocations = numberOfKeyLocations;
    }

    /**
     * @param shipmentsGroups
     * @return
     */
    protected Map<Long, List<LiteKeyLocation>> buildKeyLocations(
            final Map<Long, List<List<LiteKeyLocation>>> shipmentsGroups) {
        final Map<Long, List<LiteKeyLocation>> result = new HashMap<>();

        //convert groups to key locations
        for (final Map.Entry<Long, List<List<LiteKeyLocation>>> groups : shipmentsGroups.entrySet()) {
            final List<LiteKeyLocation> keyLocations = new LinkedList<>();
            result.put(groups.getKey(), keyLocations);

            //convert key location groups to key locations.
            for (final List<LiteKeyLocation> group : groups.getValue()) {
                keyLocations.add(getBestKeyLocation(group));
            }
        }

        return result;
    }

    /**
     * @param group key location group.
     * @return best key location.
     */
    protected LiteKeyLocation getBestKeyLocation(final List<LiteKeyLocation> group) {
        if (group.size() == 1) {
            return group.get(0);
        }

        //calculate avg temperature
        double avg = 0;
        for (final LiteKeyLocation e : group) {
            avg += e.getTemperature();
        }

        avg /= group.size();

        //select location with nearest by avg temperature
        final Iterator<LiteKeyLocation> iter = group.iterator();
        LiteKeyLocation loc = iter.next();
        double min = Math.abs(loc.getTemperature() - avg);

        while (iter.hasNext()) {
            final LiteKeyLocation next = iter.next();
            final double currentMin = Math.abs(next.getTemperature() - avg);
            if (currentMin < min) {
                min = currentMin;
                loc = next;
            }
        }

        return loc;
    }
    /**
     * @param listener the listener to set
     */
    public void setListener(final ShipmentSwitchListener listener) {
        this.listener = listener;
    }
}
