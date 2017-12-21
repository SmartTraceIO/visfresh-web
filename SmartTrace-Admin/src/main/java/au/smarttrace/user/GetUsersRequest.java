/**
 *
 */
package au.smarttrace.user;

import java.util.HashSet;
import java.util.Set;

import au.smarttrace.ctrl.req.AbstractGetRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetUsersRequest extends AbstractGetRequest {
    //filtering
    private final Set<Long> companyFilter = new HashSet<>();
    private String nameFilter;
    private String emailFilter;

    /**
     * Default constructor.
     */
    public GetUsersRequest() {
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
    public String getEmailFilter() {
        return emailFilter;
    }
    /**
     * @param emailFilter the emailFilter to set
     */
    public void setEmailFilter(final String emailFilter) {
        this.emailFilter = emailFilter;
    }
}
