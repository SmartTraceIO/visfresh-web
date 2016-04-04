/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Simulator {
    private User user;
    private Device source;
    private Device target;

    /**
     * Default constructor.
     */
    public Simulator() {
        super();
    }

    /**
     * @param u user.
     */
    public void setUser(final User u) {
        this.user = u;
    }
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    /**
     * @param d source device.
     */
    public void setSource(final Device d) {
        this.source = d;
    }
    /**
     * @return the source
     */
    public Device getSource() {
        return source;
    }
    /**
     * @param d target device.
     */
    public void setTarget(final Device d) {
        this.target = d;
    }
    /**
     * @return the target
     */
    public Device getTarget() {
        return target;
    }
}
