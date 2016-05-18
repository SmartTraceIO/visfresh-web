package com.clicksend.sdk.response;

import com.clicksend.sdk.util.Definitions;

/**
 * SmsResult.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class SmsResult implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;

    private final String statusCode;
    private final String to;
    private final String messageId;
    private final String errorText;
    private final String errorDescription;


    public SmsResult(final String statusCode,
                                  final String to,
                                  final String messageId,
                                  final String errorText) {
        this.statusCode = statusCode;
        this.to = to;
        this.messageId = messageId;
        this.errorText = errorText;
        this.errorDescription = Definitions.SEND_SMS_RESPONSE_CODES_MAP.get(statusCode);
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public String getTo() {
        return this.to;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getErrorText() {
        return this.errorText;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

	@Override
	public String toString() {
		return "SmsResult [statusCode=" + statusCode + ", to=" + to
				+ ", messageId=" + messageId + ", errorText=" + errorText
				+ ", errorDescription=" + errorDescription + "]";
	}
}
