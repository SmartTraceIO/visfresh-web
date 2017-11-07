/**
 *
 */
package com.visfresh.controllers;

import java.util.Iterator;
import java.util.List;

import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.SingleShipmentLocationBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentFilter300 {
    /**
     * Default constructor.
     */
    public SingleShipmentFilter300() {
        super();
    }

    public void filter(final List<SingleShipmentLocationBean> locations, final List<NoteBean> notes) {
        final int skipCnt = locations.size() / 300;
        int tmpCnt = 0;

        final Iterator<SingleShipmentLocationBean> iter = locations.iterator();
        boolean isFirst = true;

        while (iter.hasNext()) {
            final SingleShipmentLocationBean loc = iter.next();

            if(!isFirst){
                if(++tmpCnt <= skipCnt) {
                    //-- update shipmentNotes
                    boolean isLightEvent = false;
                    if (loc.getType().equals("LightOn") || loc.getType().equals("LightOff")) {
                        isLightEvent = true;
                    }

                    if((loc.getAlerts().size() == 0) && !isLightEvent && !checkHasNote(loc, notes)){
                        iter.remove();
                        continue;
                    }
                } else {
                    tmpCnt = 0;
                }
            } else {
                isFirst = false;
            }
        }
    }

    /**
     * @param loc
     * @param notes
     * @return
     */
    private boolean checkHasNote(final SingleShipmentLocationBean loc, final List<NoteBean> notes) {
        for (final NoteBean note: notes) {
            if (Math.abs(note.getTimeOnChart().getTime() - loc.getTime().getTime()) < 500l) {
                return true;
            }
        }
        return false;
    }
}
