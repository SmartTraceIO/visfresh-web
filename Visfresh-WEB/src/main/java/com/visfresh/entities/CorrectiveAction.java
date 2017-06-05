/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveAction {
    private String action;
    private boolean requestVerification;

    /**
     * Default constructor.
     */
    public CorrectiveAction() {
        super();
    }
    /**
     * @param action action text.
     */
    public CorrectiveAction(final String action) {
        this(action, false);
    }
    /**
     * @param action action text.
     * @param requestVerification whether or not should request verification.
     */
    public CorrectiveAction(final String action, final boolean requestVerification) {
        super();
        this.action = action;
        this.requestVerification = requestVerification;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action the action to set
     */
    public void setAction(final String action) {
        this.action = action;
    }
    /**
     * @return the requestVerification
     */
    public boolean isRequestVerification() {
        return requestVerification;
    }
    /**
     * @param requestVerification the requestVerification to set
     */
    public void setRequestVerification(final boolean requestVerification) {
        this.requestVerification = requestVerification;
    }
}
