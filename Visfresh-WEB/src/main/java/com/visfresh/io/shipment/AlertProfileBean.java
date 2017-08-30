/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.AlertProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileBean extends AlertProfileDto {
    private CorrectiveActionListBean lightOnCorrectiveActions;
    private CorrectiveActionListBean batteryLowCorrectiveActions;
    /**
     * Default constructor.
     */
    public AlertProfileBean() {
        super();
    }
    /**
     * @param ap alert profile.
     */
    public AlertProfileBean(final AlertProfile ap) {
        super(ap);
        if (ap.getLightOnCorrectiveActions() != null) {
            setLightOnCorrectiveActions(new CorrectiveActionListBean(ap.getLightOnCorrectiveActions()));
        }
        if (ap.getBatteryLowCorrectiveActions() != null) {
            setBatteryLowCorrectiveActions(new CorrectiveActionListBean(ap.getBatteryLowCorrectiveActions()));
        }
    }
    /**
     * @param actions corrective action list.
     */
    public void setBatteryLowCorrectiveActions(final CorrectiveActionListBean actions) {
        this.batteryLowCorrectiveActions = actions;
    }
    /**
     * @return the batteryLowCorrectiveActions
     */
    public CorrectiveActionListBean getBatteryLowCorrectiveActions() {
        return batteryLowCorrectiveActions;
    }
    /**
     * @param actions corrective action list.
     */
    public void setLightOnCorrectiveActions(final CorrectiveActionListBean actions) {
        this.lightOnCorrectiveActions = actions;
    }
    /**
     * @return the lightOnCorrectiveActions
     */
    public CorrectiveActionListBean getLightOnCorrectiveActions() {
        return lightOnCorrectiveActions;
    }
}
