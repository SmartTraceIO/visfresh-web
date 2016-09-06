/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Device implements EntityWithId<String>, EntityWithCompany {
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
    private Company company;
    /**
     * Current device trip count.
     */
    private int tripCount;
    /**
     * Active flag.
     */
    private boolean active = true;
    /**
     * Autostart template ID.
     */
    private Long autostartTemplateId;
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
     * @return the sn
     */
    public String getSn() {
        return getSerialNumber(getImei());
    }
    /**
     * @param imei device IMEI.
     * @return
     */
    public static String getSerialNumber(final String imei) {
        if (imei == null) {
            return null;
        }

        //normalize device serial number
        final int len = imei.length();
        final StringBuilder sb = new StringBuilder(imei.substring(len - 7, len - 1));
        while (sb.charAt(0) == '0' && sb.length() > 1) {
            sb.deleteCharAt(0);
        }

        return sb.toString();
    }
    /**
     * @param battery battery level.
     * @return converts the battery level to charge persents.
     */
    public static double batteryLevelToPersents(final int battery) {
        if (battery <= 3194.3) {
            return 0;
        } else if (battery <= 3241.6) {
            return 1;
        } else if (battery <= 3288.9) {
            return 2;
        } else if (battery <= 3336.2) {
            return 3;
        } else if (battery <= 3383.5) {
            return 4;
        } else if (battery <= 3430.8) {
            return 5;
        } else if (battery <= 3478.1) {
            return 6;
        } else if (battery <= 3525.4) {
            return 7;
        } else if (battery <= 3572.7) {
            return 8;
        } else if (battery <= 3620) {
            return 9; // start rolling out
        } else if (battery <= 3695) {
            return 10;
        } else if (battery <=3770) {
            return 20;
        } else if (battery <= 3845) {
            return 30;
        //} else if (input <= 3920) {
        } else if (battery <= 3895) {
            return 40;
        //} else if (input <= 3995) {
        } else if (battery <= 3925) {
            return 50;
        //} else if (input <= 4070) {
        } else if (battery <= 3975) {
            return 60;
        //} else if (input <= 4109) {
        } else if (battery <= 4025) {
            return 70;
        //} else if (input <= 4148) {
        } else if (battery <= 4075) {
            return 80;
        //} else if (input <= 4187) {
        } else if (battery <= 4125) {
            return 90;
        //} else if (input < 4220) {
        } else if (battery < 4150) {
            return 95;
        } else {
            return 100;
        }
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
     * @return the id
     */
    @Override
    public String getId() {
        return getImei();
    }
    /**
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @return the autostartTemplateId
     */
    public Long getAutostartTemplateId() {
        return autostartTemplateId;
    }
    /**
     * @param autostartTemplateId the autostartTemplateId to set
     */
    public void setAutostartTemplateId(final Long autostartTemplateId) {
        this.autostartTemplateId = autostartTemplateId;
    }
    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }
    /**
     * @param color the color to set
     */
    public void setColor(final Color color) {
        this.color = color;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getImei() + " (" + getDescription() + ")";
    }
}
