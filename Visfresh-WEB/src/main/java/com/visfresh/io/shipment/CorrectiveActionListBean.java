/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionListBean {
    /**
     * List ID.
     */
    private Long id;
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
    public CorrectiveActionListBean(final CorrectiveActionList list) {
        super();
        setId(list.getId());
        setName(list.getName());
        setDescription(list.getDescription());
        for (final CorrectiveAction a : list.getActions()) {
            getActions().add(new CorrectiveAction(a.getAction(), a.isRequestVerification()));
        }
    }
    /**
     * Default constructor.
     */
    public CorrectiveActionListBean() {
        super();
    }

    /**
     * @return the id
     */
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
    /**
     * @return the actions
     */
    public List<CorrectiveAction> getActions() {
        return actions;
    }
}
