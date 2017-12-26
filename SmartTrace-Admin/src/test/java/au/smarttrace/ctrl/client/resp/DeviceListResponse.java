/**
 *
 */
package au.smarttrace.ctrl.client.resp;

import au.smarttrace.Device;
import au.smarttrace.ctrl.ServiceResponse;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * Wrapper class for use it by fastxml JSON deserializer (ObjectMapper).
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceListResponse extends ServiceResponse<ListResponse<Device>> {
    /**
     * Default constructor.
     */
    public DeviceListResponse() {
        super();
    }
}
