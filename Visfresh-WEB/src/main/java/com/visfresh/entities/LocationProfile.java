/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProfile implements EntityWithId<Long>, EntityWithCompany {
    /**
     * Profile ID.
     */
    private Long id;
    /**
     * Company
     */
    private Company company;
    /**
     * Profile name.
     */
    private String name;
    /**
     * Company description
     */
    private String companyName;
    /**
     * Any notes
     */
    private String notes;
    /**
     * Address description
     */
    private String address;
    /**
     * Radius of location place.
     */
    private int radius = 500; //meters
    /**
     * Can be a start location.
     */
    private boolean start = true;
    /**
     * Can be interim location
     */
    private boolean interim = true;
    /**
     * Can be stop location
     */
    private boolean stop = true;
    /**
     * Location.
     */
    private final Location location = new Location();

    /**
     * Default constructor.
     */
    public LocationProfile() {
        super();
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
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
     * @return the company
     */
    public String getCompanyName() {
        return companyName;
    }
    /**
     * @param company the company to set
     */
    public void setCompanyName(final String company) {
        this.companyName = company;
    }
    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }
    /**
     * @param notes the notes to set
     */
    public void setNotes(final String notes) {
        this.notes = notes;
    }
    /**
     * @return the radius
     */
    public int getRadius() {
        return radius;
    }
    /**
     * @param radius the radius to set
     */
    public void setRadius(final int radius) {
        this.radius = radius;
    }
    /**
     * @return the start
     */
    public boolean isStart() {
        return start;
    }
    /**
     * @param start the start to set
     */
    public void setStart(final boolean start) {
        this.start = start;
    }
    /**
     * @return the interim
     */
    public boolean isInterim() {
        return interim;
    }
    /**
     * @param interim the interim to set
     */
    public void setInterim(final boolean interim) {
        this.interim = interim;
    }
    /**
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }
    /**
     * @param stop the stop to set
     */
    public void setStop(final boolean stop) {
        this.stop = stop;
    }
    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }
    /**
     * @return address.
     */
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(final String address) {
        this.address = address;
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
    public void setCompany(final Company company) {
        this.company = company;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId() + ": " + getAddress() + " (" + getLocation() + ")";
    }
}
