package com.clicksend.sdk.response;


/**
 * DeliveryResultItem.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class ReplyResultItem implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;

    private final String from;
    private final String customString;
    private final String message;
    private final String originalMessage;
    private final String originalMessageId;
    private final String originalSenderId;

    

    public ReplyResultItem(final String from,
    		final String originalSenderId,
    		final String message,
    		final String originalMessage,
    		final String originalMessageId,
            final String customString) {
        this.from = from;
        this.customString = customString;
        this.message = message;
        this.originalMessage = originalMessage;
        this.originalMessageId = originalMessageId;
        this.originalSenderId = originalSenderId;
    }


    public String getCustomString() {
        return this.customString;
    }

    public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}

	public String getOriginalMessage() {
		return originalMessage;
	}

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public String getOriginalSenderId() {
		return originalSenderId;
	}

	@Override
	public String toString() {
		return "ReplyResultItem [from=" + from + ", customString="
				+ customString + ", message=" + message + ", originalMessage="
				+ originalMessage + ", originalMessageId=" + originalMessageId
				+ ", originalSenderId=" + originalSenderId + "]";
	}

}
