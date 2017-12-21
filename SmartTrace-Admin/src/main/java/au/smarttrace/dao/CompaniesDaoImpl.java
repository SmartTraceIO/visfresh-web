/**
 *
 */
package au.smarttrace.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import au.smarttrace.Company;
import au.smarttrace.Language;
import au.smarttrace.PaymentMethod;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.company.CompanyConstants;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CompaniesDaoImpl extends AbstractDao implements CompaniesDao {
    private final Map<String, String> fieldAliases = new HashMap<>();

    /**
     * Default constructor.
     */
    public CompaniesDaoImpl() {
        super();
        fieldAliases.put(CompanyConstants.ID, "id");
        fieldAliases.put(CompanyConstants.NAME, "name");
        fieldAliases.put(CompanyConstants.DESCRIPTION, "description");
        fieldAliases.put(CompanyConstants.ADDRESS, "address");
        fieldAliases.put(CompanyConstants.CONTACT_PERSON, "contactperson");
        fieldAliases.put(CompanyConstants.EMAIL, "email");
        fieldAliases.put(CompanyConstants.TIME_ZONE, "timezone");
        fieldAliases.put(CompanyConstants.START_DATE, "startdate");
        fieldAliases.put(CompanyConstants.TRACKERS_EMAIL, "trackersemail");
        fieldAliases.put(CompanyConstants.PAYMENT_METHOD, "paymentmethod");
        fieldAliases.put(CompanyConstants.BILLING_PERSON, "billingperson");
        fieldAliases.put(CompanyConstants.LANGUAGE, "language");
    }

    /* (non-Javadoc)
     * @see au.smarttrace.company.CompanyDao#createCompany(au.smarttrace.Company)
     */
    @Override
    public void createCompany(final Company company) {
        final Map<String, Object> params = companyToDbMapIgnoreId(company);

        final Set<String> fields = new HashSet<>(params.keySet());
        final String sql = "insert into companies(" + String.join(",", fields) + ")"
                + " values(:" + String.join(",:", fields) + ")";

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            company.setId(keyHolder.getKey().longValue());
        }
    }

    /* (non-Javadoc)
     * @see au.smarttrace.company.CompanyDao#updateCompany(au.smarttrace.Company)
     */
    @Override
    public void updateCompany(final Company company) {
        final Map<String, Object> params = companyToDbMapIgnoreId(company);

        final List<String> sets = new LinkedList<>();
        for (final String key : params.keySet()) {
            sets.add(key + " = :" + key);
        }
        final String sql = "update companies set " + String.join(", ", sets) + " where id = :id";

        params.put("id", company.getId());
        jdbc.update(sql, params);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.company.CompanyDao#getCompanies(au.smarttrace.company.GetCompaniesRequest)
     */
    @Override
    public ListResponse<Company> getCompanies(final GetCompaniesRequest req) {
        final Map<String, Object> params = new HashMap<>();

        final List<String> where = new LinkedList<>();

        //name filter
        if (req.getNameFilter() != null) {
            final List<String> or = new LinkedList<>();
            final String[] tokens = GetListQueryUtils.tokenize(req.getNameFilter());
            for (int i = 0; i < tokens.length; i++) {
                final String t = tokens[i];
                final String key = "name_" + i;
                or.add("name like :" + key);

                params.put(key, "%" + t + "%");
            }

            if (or.size() > 0) {
                where.add("(" + String.join(" or ", or) + ")");
            }
        }

        //description filter
        if (req.getDescriptionFilter() != null) {
            final List<String> or = new LinkedList<>();
            final String[] tokens = GetListQueryUtils.tokenize(req.getDescriptionFilter());
            for (int i = 0; i < tokens.length; i++) {
                final String t = tokens[i];
                final String key = "desc_" + i;
                or.add("description like :" + key);

                params.put(key, "%" + t + "%");
            }

            if (or.size() > 0) {
                where.add("(" + String.join(" or ", or) + ")");
            }
        }

        //create query
        StringBuilder sql = new StringBuilder("select * from companies");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        //add ordering
        final List<String> orders = new LinkedList<>();
        for (final Order order : req.getOrders()) {
            final String f = fieldAliases.get(order.getField());
            //if alias exists add field to order
            if (f != null) {
                orders.add(f + (order.isAscent() ? "" : " desc"));
            }
        }

        //if order list empty, add default ordering
        if(orders.size() == 0) {
            orders.add("id");
        }
        sql.append(" order by ");
        sql.append(String.join(",", orders));

        //add limitation
        sql.append(" limit " + (req.getPage() * req.getPageSize()) + "," + req.getPageSize());

        //request data
        final ListResponse<Company> resp = new ListResponse<>();
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);
        for (final Map<String, Object> row : rows) {
            resp.getItems().add(createCompanyFromDbRow(row));
        }

        //add total count
        sql = new StringBuilder("select count(*) as totalCount from companies");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        rows = jdbc.queryForList(sql.toString(), params);
        if (rows.size() > 0) {
            resp.setTotalCount(((Number) rows.get(0).get("totalCount")).intValue());
        }

        return resp;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompaniesDao#getById(java.lang.Long)
     */
    @Override
    public Company getById(final Long id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        final List<Map<String, Object>> rows = jdbc.queryForList("select * from companies where id = :id", params);
        if (rows.size() > 0) {
            return createCompanyFromDbRow(rows.get(0));
        }
        return null;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.company.CompanyDao#deleteCompany(java.lang.Long)
     */
    @Override
    public void deleteCompany(final Long company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", company);
        jdbc.update("delete from companies where id = :id", params);
    }

    private Map<String, Object> companyToDbMapIgnoreId(final Company c) {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", c.getName());
        paramMap.put("description", c.getDescription());
        paramMap.put("address", c.getAddress());
        paramMap.put("contactperson", c.getContactPerson());
        paramMap.put("email", c.getEmail());
        paramMap.put("timezone", c.getTimeZone() == null ? null : c.getTimeZone().getID());
        paramMap.put("startdate", c.getStartDate());
        paramMap.put("trackersemail", c.getTrackersEmail());
        paramMap.put("paymentmethod", c.getPaymentMethod() == null ? null : c.getPaymentMethod().name());
        paramMap.put("billingperson", c.getBillingPerson());
        paramMap.put("language", c.getLanguage() == null ? null : c.getLanguage().name());
        return paramMap;
    }
    private Company createCompanyFromDbRow(final Map<String, Object> row) {
        final Company c = new Company();
        c.setId(((Number) row.get("id")).longValue());
        c.setName((String) row.get("name"));
        c.setDescription((String) row.get("description"));

        c.setAddress((String) row.get("address"));
        c.setContactPerson((String) row.get("contactperson"));
        c.setEmail((String) row.get("email"));

        final String tz = (String) row.get("timezone");
        if (tz != null) {
            c.setTimeZone(TimeZone.getTimeZone(tz));
        }

        c.setStartDate((Date) row.get("startdate"));
        c.setTrackersEmail((String) row.get("trackersemail"));

        final String pm = (String) row.get("paymentmethod");
        if (pm != null) {
            c.setPaymentMethod(PaymentMethod.valueOf(pm));
        }

        c.setBillingPerson((String) row.get("billingperson"));

        final String lang = (String) row.get("language");
        if (lang != null) {
            c.setLanguage(Language.valueOf(lang));
        }

        return c;
    }
}
