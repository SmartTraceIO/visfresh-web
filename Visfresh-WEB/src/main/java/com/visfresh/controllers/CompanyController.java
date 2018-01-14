/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.CompanyConstants;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Company;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.CompanySerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Company")
@RequestMapping("/rest")
public class CompanyController extends AbstractController implements CompanyConstants {
    @Autowired
    private CompanyDao dao;

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
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getCompany", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getCompany(
            @RequestParam(required = false) final Long companyId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Company company;
        if (companyId == null || user.getCompany().getId().equals(companyId)) {
            company = user.getCompany();
        } else {
            company = dao.findOne(companyId);
        }

        checkCompanyAccess(user, company);
        return createSuccessResponse(getCompanySerializer(user).toJson(company));
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return company.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getCompanies", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getCompanies(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
        final List<Company> companies = dao.findAll(null, new Sorting(getDefaultSortOrder()), page);
        final int total = dao.getEntityCount(null);

        final JsonArray array = new JsonArray();
        for (final Company c : companies) {
            array.add(getCompanySerializer(user).toJson(c));
        }
        return createListSuccessResponse(array, total);
    }
    /**
     * @param user user.
     * @return serializer.
     */
    private CompanySerializer getCompanySerializer(final User user) {
        return new CompanySerializer(user.getTimeZone());
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            NAME,
            ID,
            ADDRESS,
            CONTACT_PERSON,
            EMAIL,
            TIME_ZONE,
            START_DATE,
            TRACKERS_EMAIL,
            PAYMENT_METHOD,
            BILLING_PERSON,
            LANGUAGE
        };
    }
}
