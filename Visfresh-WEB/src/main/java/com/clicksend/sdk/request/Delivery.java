package com.clicksend.sdk.request;


/**
 * Delivery.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class Delivery implements java.io.Serializable {

    private static final long serialVersionUID = 3847700435531116012L;

    private final String messageId;

    /**
     * Creates a new delivery request.<br>
     *
     * @param messageId SMS message ID. Returned when originally sending the message.
     */
    public Delivery(final String messageId) {
    	this.messageId = messageId;
    }

    public Delivery() {
    	this.messageId = null;
    }

    public String getMessageId() {
        return this.messageId;
    }

	@Override
	public String toString() {
		return "Delivery [messageId=" + messageId + "]";
	}
    
}
