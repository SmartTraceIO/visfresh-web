package com.clicksend.sdk.response;


/**
 * DeliveryResultItem.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class DeliveryResultItem implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;

    private final String status;
    private final String customString;
    private final String messageId;
    private final String userName;


    public DeliveryResultItem(final String status,
                                  final String customString,
                                  final String messageId,final String userName) {
        this.status = status;
        this.customString = customString;
        this.messageId = messageId;
        this.userName = userName;
    }

    public String getStatusCode() {
        return this.status;
    }

    public String getCustomString() {
        return this.customString;
    }

    public String getMessageId() {
        return this.messageId;
    }

	public String getUserName() {
		return  this.userName;
	}
    
	@Override
	public String toString() {
		return "DeliveryResultItem [status=" + status + ", customString="
				+ customString + ", messageId=" + messageId + ", userName="+ userName+"]";
	}
}
