/**
 *
 */
package au.smarttrace.company;

import au.smarttrace.Company;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface CompaniesService {
    /**
     * @param company company to create.
     */
    void createCompany(Company company);
    /**
     * @param company company to save.
     */
    void updateCompany(Company company);
    /**
     * @param req request for get filtered company list.
     * @return list of company
     */
    ListResponse<Company> getCompanies(GetCompaniesRequest req);
    /**
     * Deletes company with given ID.
     * @param company company ID to delete.
     */
    void deleteCompany(Long company);
    /**
     * @param id company ID.
     * @return company.
     */
    Company getCompany(Long id);
}
