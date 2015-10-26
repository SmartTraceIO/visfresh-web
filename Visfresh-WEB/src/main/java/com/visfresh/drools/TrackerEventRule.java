/**
 *
 */
package com.visfresh.drools;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TrackerEventRule {
    /**
     * @param e tracker event.
     * @return true if can process event.
     */
    boolean accept(TrackerEventRequest e);
    /**
     * @param e tracker event.
     * @return true if need reprocess event.
     */
    boolean handle(TrackerEventRequest e);
}
