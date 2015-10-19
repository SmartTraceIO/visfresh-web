/**
 *
 */
package com.visfresh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceConstants {
    private String deviceName;
    private String accountId;

    /**
     * Default constructor.
     */
    public DeviceConstants() {
        super();
    }

    /**
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }
    /**
     * @param deviceName
     *            the deviceName to set
     */
    @Value("${resolvedMessages.deviceName}")
    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }
    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }
    /**
     * @param accountId the accountId to set
     */
    @Value("${deviceAccountId}")
    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }
}
