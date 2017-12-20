/**
 *
 */
package au.smarttrace.ctrl.client;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class IoLogger implements IoListener {
    /**
     * Default constructor.
     */
    public IoLogger() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.restclient.RestIoListener#sendingRequest(java.lang.String, java.lang.String)
     */
    @Override
    public void sendingRequest(final String url, final String body, final String methodName) {
        System.out.println(methodName + " " + url);
        if (body != null) {
            System.out.println("Request:");
            System.out.println(body);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.restclient.RestIoListener#receivingResponse(java.lang.String)
     */
    @Override
    public void receivedResponse(final String responseBody) {
        System.out.println("Response:");
        System.out.println(responseBody);
    }
}
