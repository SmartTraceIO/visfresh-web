/**
 *
 */
package com.visfresh.tools;

import com.visfresh.logs.LogUnit;
import com.visfresh.tracker.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ExtractedMessageHandler {
    void handle(LogUnit u, DeviceMessage m);
}
