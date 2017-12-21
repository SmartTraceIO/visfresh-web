/**
 *
 */
package au.smarttrace.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.smarttrace.Company;
import au.smarttrace.Roles;
import au.smarttrace.company.CompaniesService;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("companies")
@RequestMapping(produces = "application/json;charset=UTF-8")
public class CompaniesController {
    @Autowired
    private CompaniesService service;

    /**
     * Default constructor.
     */
    public CompaniesController() {
        super();
    }

    /**
     * @param company company to create.
     * @return company ID.
     */
    @RequestMapping(value = "/createCompany", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Long createCompany(final @RequestBody Company company) {
        service.createCompany(company);
        return company.getId();
    }
    /**
     * @param company company to create.
     * @return company ID.
     */
    @RequestMapping(value = "/updateCompany", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Long updateCompany(final @RequestBody Company company) {
        service.updateCompany(company);
        return company.getId();
    }
    /**
     * @param req request for get companies.
     * @return list of company.
     */
    @RequestMapping(value = "/getCompanies", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public ListResponse<Company> updateCompany(final @RequestBody GetCompaniesRequest req) {
        return service.getCompanies(req);
    }
    @RequestMapping(value = "/deleteCompany", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public String deleteCompany(final @RequestParam Long company) {
        service.deleteCompany(company);
        return "OK";
    }
    @RequestMapping(value = "/getCompany", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Company getCompany(final @RequestParam Long id) {
        return service.getCompany(id);
    }
}
