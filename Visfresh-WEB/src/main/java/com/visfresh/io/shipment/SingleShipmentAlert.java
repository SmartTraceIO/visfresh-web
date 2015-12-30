/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentAlert {
    private String title;
    private final List<String> lines = new LinkedList<String>();
    private String type;

    /**
     * Default constructor.
     */
    public SingleShipmentAlert() {
        super();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }
    /**
     * @return the lines
     */
    public List<String> getLines() {
        return lines;
    }
}
