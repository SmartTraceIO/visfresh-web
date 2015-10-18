/**
 *
 */
package com.visfresh.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="locationprofiles")
public class LocationProfile implements EntityWithId {
    /**
     * Profile ID.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    /**
     * Company
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "company",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private Company company;
    /**
     * Profile name.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Company description
     */
    @Column
    private String companyDescription;
    /**
     * Any notes
     */
    @Column
    private String notes;
    /**
     * Address description
     */
    @Column(nullable = false)
    private String address;
    /**
     * Radius of location place.
     */
    @Column
    private int radius = 500; //meters
    /**
     * Can be a start location.
     */
    @Column
    private boolean start = true;
    /**
     * Can be interim location
     */
    @Column
    private boolean interim = true;
    /**
     * Can be stop location
     */
    @Column
    private boolean stop = true;
    /**
     * Location.
     */
    @Embedded
    @AttributeOverrides(
            {@AttributeOverride(name = "latitude", column = @Column(nullable = false))
            , @AttributeOverride(name = "longitude", column = @Column(nullable = false))
            })
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
    public String getCompanyDescription() {
        return companyDescription;
    }
    /**
     * @param company the company to set
     */
    public void setCompanyDescription(final String company) {
        this.companyDescription = company;
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
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
}
