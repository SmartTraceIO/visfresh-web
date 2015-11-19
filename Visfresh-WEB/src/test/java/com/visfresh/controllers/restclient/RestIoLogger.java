/**
 *
 */
package com.visfresh.controllers.restclient;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestIoLogger implements RestIoListener {
    /**
     * Default constructor.
     */
    public RestIoLogger() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.restclient.RestIoListener#sendingRequest(java.lang.String, java.lang.String)
     */
    @Override
    public void sendingRequest(final String url, final String body, final String methodName) {
        System.out.println(methodName + " " + url);
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
