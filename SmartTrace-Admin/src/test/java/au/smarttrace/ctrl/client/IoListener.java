/**
 *
 */
package au.smarttrace.ctrl.client;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface IoListener {
    void sendingRequest(String url, String body, String methodName);
    void receivedResponse(String responseBody);
}
