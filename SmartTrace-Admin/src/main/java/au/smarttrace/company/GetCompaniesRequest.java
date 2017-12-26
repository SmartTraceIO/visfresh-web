/**
 *
 */
package au.smarttrace.company;

import au.smarttrace.ctrl.req.AbstractGetListRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetCompaniesRequest extends AbstractGetListRequest {
    private String nameFilter;
    private String descriptionFilter;

    /**
     * Default constructor.
     */
    public GetCompaniesRequest() {
        super();
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
     * @param filter filter.
     */
    public void setDescriptionFilter(final String filter) {
        this.descriptionFilter = filter;
    }
    /**
     * @return the descriptionFilter
     */
    public String getDescriptionFilter() {
        return descriptionFilter;
    }
}
