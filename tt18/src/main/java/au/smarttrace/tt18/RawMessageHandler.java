/**
 *
 */
package au.smarttrace.tt18;

import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RawMessageHandler {
    List<String> handleMessage(RawMessage msg);
}
