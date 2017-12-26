/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.smarttrace.Company;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.client.resp.AnyResponse;
import au.smarttrace.ctrl.client.resp.CompanyListResponse;
import au.smarttrace.ctrl.client.resp.CompanyResponse;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompaniesClient extends BaseClient {
    /**
     * Default constructor.
     */
    public CompaniesClient() {
        super();
    }

    /**
     * @param id company ID.
     * @throws ServiceException
     * @throws IOException
     */
    public void deleteCompany(final Long id) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("company", id.toString());
        sendGetRequest(getPathWithToken("deleteCompany"), params, AnyResponse.class);
    }

    /**
     * @param company company to create.
     * @throws ServiceException
     * @throws IOException
     */
    public Long createCompany(final Company company) throws IOException, ServiceException {
        final Object obj = sendPostRequest(getPathWithToken("createCompany"), company, AnyResponse.class);
        return Long.valueOf(obj.toString());
    }
    /**
     * @param id company ID.
     * @return company.
     * @throws ServiceException
     * @throws IOException
     */
    public Company getCompany(final Long id) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("id", id.toString());
        return sendGetRequest(getPathWithToken("getCompany"), params, CompanyResponse.class);
    }
    /**
     * @param req get companies request.
     * @return list of companies.
     * @throws IOException
     * @throws ServiceException
     */
    public ListResponse<Company> getCompanies(final GetCompaniesRequest req) throws IOException, ServiceException {
        return sendPostRequest(getPathWithToken("getCompanies"), req, CompanyListResponse.class);
    }
    /**
     * @param company
     * @throws ServiceException
     * @throws IOException
     */
    public void updateCompany(final Company company) throws IOException, ServiceException {
        sendPostRequest(getPathWithToken("updateCompany"), company, AnyResponse.class);
    }
}
