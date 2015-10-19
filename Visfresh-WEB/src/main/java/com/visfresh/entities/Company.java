/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Company implements EntityWithId<Long> {
    /**
     * Company ID.
     */
    private Long id;

    /**
     * Company name.
     */
    private String name;
    /**
     * Company description
     */
    private String description;

    /**
     * Default constructor.
     */
    public Company() {
        super();
    }

    /**
     * @param l
     */
    public Company(final long l) {
        super();
        setId(l);
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
}
