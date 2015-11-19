/**
 *
 */
package com.visfresh.controllers.restclient;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestIoListener {
    void sendingRequest(String url, String body, String methodName);
    void receivedResponse(String responseBody);
}
