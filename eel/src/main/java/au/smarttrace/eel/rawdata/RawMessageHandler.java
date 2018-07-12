/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RawMessageHandler {
    List<String> handleMessage(EelMessage msg);
}
