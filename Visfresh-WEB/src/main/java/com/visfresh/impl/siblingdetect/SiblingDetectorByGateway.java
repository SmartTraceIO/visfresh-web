/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.HashSet;
import java.util.Set;

import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectorByGateway extends StateFullSiblingDetector {
    private final Set<String> g1 = new HashSet<>();
    private final Set<String> g2 = new HashSet<>();

    /**
     * @param direction
     */
    public SiblingDetectorByGateway() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.StateFullSiblingDetector#doNext(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
     */
    @Override
    protected void doNext(final TrackerEventDto e1, final TrackerEventDto e2) {
        if (e2 == null) {
            g2.add(e1.getGateway());
            if (e1.getGateway() != null && g2.contains(e1.getGateway())) {
                setState(State.Siblings);
            }
        } else if (e1 == null) {
            g1.add(e2.getGateway());
            if (e2.getGateway() != null && g1.contains(e2.getGateway())) {
                setState(State.Siblings);
            }
        } else {
            g1.add(e1.getGateway());
            g2.add(e2.getGateway());

            if (e1.getGateway() != null && g2.contains(e1.getGateway())) {
                setState(State.Siblings);
            }
            if (e2.getGateway() != null && g1.contains(e2.getGateway())) {
                setState(State.Siblings);
            }
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.StateFullSiblingDetector#doFinish()
     */
    @Override
    protected void doFinish() {
        g1.clear();
        g2.clear();
    }
}
