/**
 *
 */
package com.visfresh.controllers;

import java.util.Iterator;
import java.util.List;

import com.visfresh.io.NoteDto;
import com.visfresh.io.shipment.SingleShipmentLocation;

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

    public void filter(final List<SingleShipmentLocation> locations, final List<NoteDto> notes) {
        final int skipCnt = locations.size() / 300;
        int tmpCnt = 0;

        final Iterator<SingleShipmentLocation> iter = locations.iterator();
        boolean isFirst = true;

        while (iter.hasNext()) {
            final SingleShipmentLocation loc = iter.next();

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
    private boolean checkHasNote(final SingleShipmentLocation loc, final List<NoteDto> notes) {
        for (final NoteDto note: notes) {
            if (note.getTimeOnChart().equals(loc.getTimeIso())) {
                return true;
            }
        }
        return false;
    }
}
