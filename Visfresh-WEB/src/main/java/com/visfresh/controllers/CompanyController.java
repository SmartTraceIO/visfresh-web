/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.services.ReportService;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Company")
@RequestMapping("/rest")
public class CompanyController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;
    /**
     * Report service.
     */
    @Autowired
    private ReportService reportService;

    /**
     * Default constructor.
     */
    public CompanyController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param companyId company ID.
     * @return company.
     */
    @RequestMapping(value = "/getCompany/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getCompany(@PathVariable final String authToken,
            @RequestParam final Long companyId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetCompany(user, companyId);

            final Company company;
            if (user.getCompany().getId().equals(companyId)) {
                company = user.getCompany();
            } else {
                company = restService.getCompany(companyId);
            }
            return createSuccessResponse(getSerializer(user).toJson(company));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return company.
     */
    @RequestMapping(value = "/getCompanies/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getCompanies(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetCompanies(user);

            final List<Company> company = getPage(restService.getCompanies(), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Company c : company) {
                array.add(getSerializer(user).toJson(c));
            }
            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
}
