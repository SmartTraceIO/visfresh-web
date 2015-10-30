/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAlertProfileDao extends MockDaoBase<AlertProfile, Long> implements AlertProfileDao {
    /**
     * Default constructor.
     */
    public MockAlertProfileDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.LocationProfileDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<AlertProfile> findByCompany(final Company company) {
        final List<AlertProfile> list = new LinkedList<AlertProfile>();
        for (final AlertProfile d : entities.values()) {
            if (d.getCompany().getId().equals(company.getId())) {
                list.add(d);
            }
        }
        return list;
    }
}
