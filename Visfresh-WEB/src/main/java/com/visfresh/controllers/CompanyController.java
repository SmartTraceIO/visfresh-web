/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.visfresh.entities.User;
import com.visfresh.io.json.CompanySerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Company")
@RequestMapping("/rest")
public class CompanyController extends AbstractController implements CompanyConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

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
     */
    @RequestMapping(value = "/getCompany/{authToken}", method = RequestMethod.GET)
    public JsonObject getCompany(@PathVariable final String authToken,
            @RequestParam(required = false) final Long companyId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            if (companyId != null) {
                security.checkCanGetCompany(user, companyId);
            }

            final Company company;
            if (companyId == null || user.getCompany().getId().equals(companyId)) {
                company = user.getCompany();
            } else {
                company = dao.findOne(companyId);
            }
            return createSuccessResponse(getCompanySerializer(user).toJson(company));
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
    public JsonObject getCompanies(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetCompanies(user);

            final List<Company> companies = dao.findAll(null, new Sorting(getDefaultSortOrder()), page);
            final int total = dao.getEntityCount(null);

            final JsonArray array = new JsonArray();
            for (final Company c : companies) {
                array.add(getCompanySerializer(user).toJson(c));
            }
            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
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
            PROPERTY_NAME,
            PROPERTY_ID
        };
    }
}
