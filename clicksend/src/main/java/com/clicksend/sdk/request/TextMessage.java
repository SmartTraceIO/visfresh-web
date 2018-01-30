package com.clicksend.sdk.request;


/**
 * TextMessage.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class TextMessage extends Message {

    static final long serialVersionUID = 6258872793039443129L;

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
     */
    public TextMessage(final String to,
                       final String message) {
        super(to,
        		message,
                null,
                null,
                null,
                null);
    }
    
    

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
     * @param senderId	custom sender ID:
						-Alphanumeric e.g. "MyCompany". 11 characters max. No spaces. The recipient will not be able to reply to the message.
						-Numeric e.g. +61411111111. You can enter your own mobile number in international format to make messages appear to come from your mobile number. Replies will be sent directly to your mobile.
						-Leave blank for two-way SMS. Replies will be directed back to the original sender. 

     */
    public TextMessage(final String to,final String message,final String senderId) {
        super(to,message,senderId,
                null,
                null,
                null);
    }  
    
    
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
    public TextMessage(final String to,
            final String message,
            final String senderId,
            final String schedule,
            final String customString,
            final String returnz
            ) {
        super(
              to,
              message,
              senderId,
              schedule,
              customString,
              returnz);
    }


	@Override
	public String toString() {
		return "TextMessage []";
	}
}
