/**
 *
 */
package au.smarttrace.geolocation;

import au.smarttrace.geolocation.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestMessage {
    private String field;
    private Location location;

    /**
     * Default constructor.
     */
    public TestMessage() {
        super();
    }

    /**
     * @param field the field to set
     */
    public void setField(final String field) {
        this.field = field;
    }
    /**
     * @return the field
     */
    public String getField() {
        return field;
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
}
