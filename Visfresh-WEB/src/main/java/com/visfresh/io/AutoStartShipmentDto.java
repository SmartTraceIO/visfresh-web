/**
 *
 */
package com.visfresh.io;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentDto {
    /**
     * The list of start location ID.
     */
    private final List<Long> startLocations = new LinkedList<>();
    /**
     * The list of end location ID.
     */
    private final List<Long> endLocations = new LinkedList<>();
    private Long template;
    private Long id;

    /**
     * Default constructor.
     */
    public AutoStartShipmentDto() {
        super();
    }
    /**
     * @param cfg default shipment configuration.
     */
    public AutoStartShipmentDto(final AutoStartShipment cfg) {
        super();

        setId(cfg.getId());
        setTemplate(cfg.getTemplate().getId());

        //add start locations.
        for (final LocationProfile loc : cfg.getShippedFrom()) {
            startLocations.add(loc.getId());
        }
        //add end locations.
        for (final LocationProfile loc : cfg.getShippedTo()) {
            endLocations.add(loc.getId());
        }
    }
    /**
     * @return the list of locations.
     */
    public List<Long> getStartLocations() {
        return startLocations;
    }
    /**
     * @return the template
     */
    public Long getTemplate() {
        return template;
    }
    /**
     * @param template the template to set
     */
    public void setTemplate(final Long template) {
        this.template = template;
    }
    /**
     * @return list of end locations.
     */
    public List<Long> getEndLocations() {
        return endLocations;
    }
    /**
     * @param id default shipment config ID.
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
}
