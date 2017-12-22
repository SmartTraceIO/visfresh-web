/**
 *
 */
package au.smarttrace.device;

import java.util.HashSet;
import java.util.Set;

import au.smarttrace.ctrl.req.AbstractGetRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetDevicesRequest extends AbstractGetRequest {
    //filtering
    private final Set<Long> companyFilter = new HashSet<>();
    private String nameFilter;
    private String imeiFilter;

    /**
     * Default constructor.
     */
    public GetDevicesRequest() {
        super();
    }

    /**
     * @return the companies filter.
     */
    public Set<Long> getCompanyFilter() {
        return companyFilter;
    }
    /**
     * @return the nameFilter
     */
    public String getNameFilter() {
        return nameFilter;
    }
    /**
     * @param nameFilter the nameFilter to set
     */
    public void setNameFilter(final String nameFilter) {
        this.nameFilter = nameFilter;
    }
    /**
     * @return the emailFilter
     */
    public String getImeiFilter() {
        return imeiFilter;
    }
    /**
     * @param emailFilter the emailFilter to set
     */
    public void setImeiFilter(final String emailFilter) {
        this.imeiFilter = emailFilter;
    }
}
