/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.visfresh.utils.CollectionUtils;

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
    private Long company;
    /**
     * Action list name.
     */
    private String name;
    /**
     * Action list name.
     */
    private String description;
    /**
     * List of actions.
     */
    private final List<CorrectiveAction> actions = new LinkedList<>();

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
    public Long getCompanyId() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Long company) {
        this.company = company;
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
     * @return the actions
     */
    public List<CorrectiveAction> getActions() {
        return actions;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CorrectiveActionList)) {
            return false;
        }

        final CorrectiveActionList other = (CorrectiveActionList) obj;
        return Objects.equals(getId(), other.getId())
                && getCompanyId().equals(other.getCompanyId())
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getDescription(), other.getDescription())
                && CollectionUtils.equals(getActions(), other.getActions());
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final List<Object> hash = new LinkedList<>();
        hash.add(getId());
        hash.add(getName());
        hash.add(getDescription());
        hash.add(getCompanyId());

        for (final CorrectiveAction a : getActions()) {
            hash.add(a);
        }

        return Objects.hash(hash.toArray(new Object[hash.size()]));
    }
}
