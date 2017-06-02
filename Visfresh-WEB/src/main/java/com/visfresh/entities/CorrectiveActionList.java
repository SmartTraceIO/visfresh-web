/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionList implements EntityWithId<Long>, EntityWithCompany {
    /**
     * List ID.
     */
    private Long id;
    /**
     * ID in data base.
     */
    private Company company;
    /**
     * Action list name.
     */
    private String name;
    /**
     * List of actions.
     */
    private final List<String> actions = new LinkedList<>();

    /**
     * Default constructor.
     */
    public CorrectiveActionList() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
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
     * @return the actions
     */
    public List<String> getActions() {
        return actions;
    }
}
