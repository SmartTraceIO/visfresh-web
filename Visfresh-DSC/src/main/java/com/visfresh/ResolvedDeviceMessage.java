/**
 *
 */
package com.visfresh;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ResolvedDeviceMessage extends DeviceMessageBase {
    private Location location;

    /**
     * Default constructor.
     */
    public ResolvedDeviceMessage() {
        super();
    }
    /**
     * @param m device message.
     */
    public ResolvedDeviceMessage(final DeviceMessage m) {
        super(m);
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final Location location) {
        this.location = location;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString()).append('\n');
        sb.append(getLocation().getLatitude()).append('|').append(getLocation().getLongitude()).append('|');
        return sb.toString();
    }
}
