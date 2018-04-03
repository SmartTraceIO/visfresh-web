/**
 *
 */
package au.smarttrace;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Device {
    /**
     * Device IMEI code
     */
    private String imei;
    /**
     * Device name.
     */
    private String name;
    /**
     * Device description
     */
    private String description;
    /**
     * Is active flag.
     */
    private boolean isActive;

    /**
     * Default constructor.
     */
    public Device() {
        super();
    }

    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }
    /**
     * @param isActive the isActive to set
     */
    public void setActive(final boolean isActive) {
        this.isActive = isActive;
    }
}
