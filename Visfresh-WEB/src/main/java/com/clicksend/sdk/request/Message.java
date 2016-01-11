package com.clicksend.sdk.request;


/**
 * Message.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public abstract class Message implements java.io.Serializable {

    private static final long serialVersionUID = 3847700435531116012L;

    private final String senderId;
    private final String to;
    private final int recipientCount;
	private final String message;
    private final String schedule;
    private final String customString;
    private final String returnz;

    /**
     * Creates a new SMS request.<br>
     *
     * @param to	Recipient Mobile Number in international format (with leading + and country code). Separate multiple recipients with a comma (,) where applicable. Maximum 1000 recipients.
					For example:

    				+614XXXXXXXX (Australia)
    				+1XXXXXXXXXX (US)
    				+65XXXXXXXXX (Singapore)
    				+44XXXXXXXXXX (UK)

     * @param message	The message to be sent. Maximum 960 characters.
     * @param isUnicode	For non-English characters use true
						set false for a standard English message.
     * @param senderId	custom sender ID:
						-Alphanumeric e.g. "MyCompany". 11 characters max. No spaces. The recipient will not be able to reply to the message.
						-Numeric e.g. +61411111111. You can enter your own mobile number in international format to make messages appear to come from your mobile number. Replies will be sent directly to your mobile.
						-Leave blank for two-way SMS. Replies will be directed back to the original sender. 

     * @param schedule	Allows you to schedule message delivery. Must be in unix format.
						For example: 1348742950.
						Leave blank for instant delivery.
            
     * @param customString	A custom string that will be passed back with replies and delivery reports. Maximum 50 characters.

     * @param returnz	Redirect to a URL after delivering the message(s).
     */
    protected Message(final String to,
                      final String message,
                      final String senderId,
                      final String schedule,
                      final String customString,
                      final String returnz
) {
    	this.to = to;
        this.recipientCount = this.to.split(",").length;
        
        this.message = message;
        
        this.senderId = senderId;
        this.schedule = schedule;
        this.customString = customString;
        this.returnz = returnz;
        

    }

    public int getRecipientCount() {
		return recipientCount;
	}


    public String getSenderId() {
        return this.senderId;
    }

    public String getTo() {
        return this.to;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSchedule() {
		return schedule;
	}

	public String getCustomString() {
		return customString;
	}

	public String getReturn() {
		return returnz;
	}

	@Override
	public String toString() {
		return "Message [senderId=" + senderId + ", to=" + to
				+ ", recipientCount=" + recipientCount + ", message=" + message
				+ ", schedule=" + schedule + ", customString=" + customString
				+ ", returnz=" + returnz + "]";
	}

}
