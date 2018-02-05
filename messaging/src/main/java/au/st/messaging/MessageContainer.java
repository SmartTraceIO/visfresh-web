/**
 *
 */
package au.st.messaging;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageContainer {
    private Long id;
    private String type;
    private String group;
    private Date time;
    private int numretry;
    private String message;
    private Date retryon;
    private String processor;

    /**
     * Default constructor.
     */
    public MessageContainer() {
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
     * @return the processor
     */
    public String getProcessor() {
        return processor;
    }
    /**
     * @param processor the processor to set
     */
    public void setProcessor(final String processor) {
        this.processor = processor;
    }
    /**
     * @return the retryon
     */
    public Date getRetryon() {
        return retryon;
    }
    /**
     * @param retryon the retryon to set
     */
    public void setRetryon(final Date retryon) {
        this.retryon = retryon;
    }
    /**
     * @return the numretry
     */
    public int getNumretry() {
        return numretry;
    }
    /**
     * @param numretry the numretry to set
     */
    public void setNumretry(final int numretry) {
        this.numretry = numretry;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }
}
