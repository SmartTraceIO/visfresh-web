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
public class DeliveryResult implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;

    private final String result;
    private final String errorText;
    private final String errorDescription;
    private final List<DeliveryResultItem> deliveryResultItems;


    public DeliveryResult(final String result,
                                  final String errorText,
                                  final List<DeliveryResultItem> deliveryResultItems) {
        this.result = result;
        this.errorText = errorText;
        this.deliveryResultItems = deliveryResultItems;
        this.errorDescription = Definitions.GET_DLR_RESPONSE_CODES_MAP.get(result);
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

    public List<DeliveryResultItem> getDeliveryResultItems() {
        return this.deliveryResultItems;
    }

	@Override
	public String toString() {
		return "DeliveryResult [result=" + result + ", errorText=" + errorText
				+ ", errorDescription=" + errorDescription
				+ ", deliveryResultItems=" + deliveryResultItems + "]";
	}
}
