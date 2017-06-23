/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessage implements EntityWithId<Long> {
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
    private SystemMessageType type;
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
     * Message info.
     */
    private String messageInfo;
    /**
     * Current message processor.
     */
    private String processor;

    /**
     * Default constructor.
     */
    public SystemMessage() {
        super();
    }

    /**
     * @return the type
     */
    public SystemMessageType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final SystemMessageType type) {
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
     * @return the readyOn
     */
    public Date getRetryOn() {
        return retryOn;
    }
    /**
     * @param retryOn the readyOn to set
     */
    public void setRetryOn(final Date retryOn) {
        this.retryOn = retryOn;
    }
    /**
     * @return the id
     */
    @Override
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
     * @return the messageInfo
     */
    public String getMessageInfo() {
        return messageInfo;
    }
    /**
     * @param messageInfo the messageInfo to set
     */
    public void setMessageInfo(final String messageInfo) {
        this.messageInfo = messageInfo;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getType() + "(" + getTime() +"): " + getMessageInfo();
    }
}
