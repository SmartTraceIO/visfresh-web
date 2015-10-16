/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name = "companies")
public class Company implements EntityWithId {
    private Long id;
    /**
     * Company name.
     */
    private String name;

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
}
