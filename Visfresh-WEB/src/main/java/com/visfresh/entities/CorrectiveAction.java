/**
 *
 */
package com.visfresh.entities;

import java.util.Objects;

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
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CorrectiveAction)) {
            return false;
        }

        final CorrectiveAction other = (CorrectiveAction) obj;
        return Objects.equals(getAction(), other.getAction())
                && isRequestVerification() == other.isRequestVerification();
    }
}
