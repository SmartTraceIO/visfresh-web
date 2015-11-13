/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.entities.Company;
import com.visfresh.io.json.CompanySerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyRestClient extends RestClient {
    private CompanySerializer serializer;

    /**
     * @param tz time zone.
     */
    public CompanyRestClient(final TimeZone tz) {
        super();
        this.serializer = new CompanySerializer(tz);
    }

    /**
     * @param id company ID.
     * @return Company
     * @throws RestServiceException
     * @throws IOException
     */
    public Company getCompany(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("companyId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getCompany"),
                params);
        return serializer.parseCompany(response);
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Company> getCompanies(final Integer pageIndex, final Integer pageSize) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        final JsonArray response = sendGetRequest(getPathWithToken("getCompanies"), params).getAsJsonArray();

        final List<Company> result = new LinkedList<Company>();
        for (final JsonElement e : response) {
            result.add(serializer.parseCompany(e));
        }
        return result;
    }
}
