/**
 *
 */
package au.smarttrace.eel.service;

import au.smarttrace.eel.rawdata.EelMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface EelMessageHandler {
    void handleMessage(EelMessage msg);
}
