/**
 *
 */
package au.smarttrace;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

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
     * Company
     */
    private Long company;
    /**
     * Active flag.
     */
    private boolean active = true;
    /**
     * Device color.
     */
    private Color color;

    /**
     * Default constructor.
     */
    public Device() {
        super();
    }

    /**
     * @return the imei
     */
    @JsonGetter("imei")
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    @JsonSetter("imei")
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the name
     */
    @JsonGetter("name")
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    @JsonSetter("name")
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    @JsonGetter("description")
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    @JsonSetter("description")
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the company
     */
    @JsonGetter("company")
    public Long getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @JsonSetter("company")
    public void setCompany(final Long company) {
        this.company = company;
    }
    /**
     * @return the active
     */
    @JsonGetter("active")
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    @JsonSetter("active")
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the color
     */
    @JsonGetter("color")
    public Color getColor() {
        return color;
    }
    /**
     * @param color the color to set
     */
    @JsonSetter("color")
    public void setColor(final Color color) {
        this.color = color;
    }
}
