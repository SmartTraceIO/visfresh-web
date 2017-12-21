/**
 *
 */
package au.smarttrace.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.Company;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.company.CompaniesService;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CompaniesServiceImpl implements CompaniesService {
    @Autowired
    private CompaniesDao dao;

    /**
     * Default constructor.
     */
    public CompaniesServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesService#createCompany(au.smarttrace.Company)
     */
    @Override
    public void createCompany(final Company company) {
        dao.createCompany(company);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesService#updateCompany(au.smarttrace.Company)
     */
    @Override
    public void updateCompany(final Company company) {
        dao.updateCompany(company);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesService#getCompanies(au.smarttrace.ctrl.req.GetCompaniesRequest)
     */
    @Override
    public ListResponse<Company> getCompanies(final GetCompaniesRequest req) {
        return dao.getCompanies(req);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesService#deleteCompany(java.lang.Long)
     */
    @Override
    public void deleteCompany(final Long company) {
        dao.deleteCompany(company);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesService#getCompany(java.lang.Long)
     */
    @Override
    public Company getCompany(final Long id) {
        return dao.getById(id);
    }
}
