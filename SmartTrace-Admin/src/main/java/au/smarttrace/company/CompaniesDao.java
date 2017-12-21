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
public interface CompaniesDao {
    /**
     * @param company company to create.
     */
    void createCompany(Company company);
    /**
     * @param company company to update.
     */
    void updateCompany(Company company);
    /**
     * @param req request for get company list.
     * @return company list.
     */
    ListResponse<Company> getCompanies(GetCompaniesRequest req);
    /**
     * Deletes company by given ID.
     * @param company company ID to delete.
     */
    void deleteCompany(Long company);
    /**
     * @param id company ID.
     * @return company with given ID.
     */
    Company getById(Long id);
}
