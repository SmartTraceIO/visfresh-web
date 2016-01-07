/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultSiblingDetector implements SiblingDetector {

    /**
     * Default constructor.
     */
    public DefaultSiblingDetector() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.SiblingDetector#getSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Shipment> getSiblings(final Shipment shipment) {
        return new LinkedList<>();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblingCount(com.visfresh.entities.Shipment)
     */
    @Override
    public int getSiblingCount(final Shipment s) {
        return getSiblings(s).size();
    }

    public void detectSiblings() {

    }
}
