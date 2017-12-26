/**
 *
 */
package au.smarttrace.ctrl.client.resp;

import au.smarttrace.Company;
import au.smarttrace.ctrl.ServiceResponse;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * Wrapper class for use it by fastxml JSON deserializer (ObjectMapper).
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyListResponse extends ServiceResponse<ListResponse<Company>> {
    /**
     * Default constructor.
     */
    public CompanyListResponse() {
        super();
    }
}
