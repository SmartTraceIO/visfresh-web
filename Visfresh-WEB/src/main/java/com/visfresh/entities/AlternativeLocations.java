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
public class AlternativeLocations {
    private final List<LocationProfile> from = new LinkedList<>();
    private final List<LocationProfile> to = new LinkedList<>();
    private final List<LocationProfile> interim = new LinkedList<>();

    /**
     * Default constructor.
     */
    public AlternativeLocations() {
        super();
    }

    /**
     * @return the from
     */
    public List<LocationProfile> getFrom() {
        return from;
    }
    /**
     * @return the to
     */
    public List<LocationProfile> getTo() {
        return to;
    }
    /**
     * @return the interim
     */
    public List<LocationProfile> getInterim() {
        return interim;
    }

    /**
     * @return true if is empty.
     */
    public boolean isEmpty() {
        return getFrom().isEmpty() && getTo().isEmpty() && getInterim().isEmpty();
    }
}
