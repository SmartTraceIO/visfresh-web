/**
 *
 */
package au.st.messaging;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessage {
    /**
     * The message ID.
     */
    private Long id;
    /**
     * System message group.
     */
    private String group;
    /**
     * Message type.
     */
    private String type;
    /**
     * Time of creation.
     */
    private Date time;
    /**
     * The number of retry.
     */
    private int numberOfRetry;
    /**
     * The ready on date.
     */
    private Date retryOn = new Date();
    /**
     * Message payload.
     */
    private String payload;

    /**
     * Default constructor.
     */
    public SystemMessage() {
        super();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }
    /**
     * @param group the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
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
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
    /**
     * @return the numberOfRetry
     */
    public int getNumberOfRetry() {
        return numberOfRetry;
    }
    /**
     * @param numberOfRetry the numberOfRetry to set
     */
    public void setNumberOfRetry(final int numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
    }
    /**
     * @return the retryOn
     */
    public Date getRetryOn() {
        return retryOn;
    }
    /**
     * @param retryOn the retryOn to set
     */
    public void setRetryOn(final Date retryOn) {
        this.retryOn = retryOn;
    }
    /**
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }
    /**
     * @param payload the payload to set
     */
    public void setPayload(final String payload) {
        this.payload = payload;
    }
}
