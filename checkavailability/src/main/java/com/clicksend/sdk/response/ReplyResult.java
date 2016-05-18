package com.clicksend.sdk.response;

import java.util.List;

import com.clicksend.sdk.util.Definitions;


/**
 * DeliveryResult.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class ReplyResult implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;

    private final String result;
    private final String errorText;
    private final String errorDescription;
    private final List<ReplyResultItem> replyResultItems;


    public ReplyResult(final String result,
                                  final String errorText,
                                  final List<ReplyResultItem> replyResultItems) {
        this.result = result;
        this.errorText = errorText;
        this.replyResultItems = replyResultItems;
        this.errorDescription = Definitions.GET_REPLY_RESPONSE_CODES_MAP.get(result);
    }
    
    public String getErrorDescription() {
        return this.errorDescription;
    }

    public String getResult() {
        return this.result;
    }

    public String getErrorText() {
        return this.errorText;
    }

    public List<ReplyResultItem> getReplyResultItems() {
        return this.replyResultItems;
    }

    @Override
	public String toString() {
		return "ReplyResult [result=" + result + ", errorText=" + errorText
				+ ", errorDescription=" + errorDescription
				+ ", replyResultItems=" + replyResultItems + "]";
	}

}
