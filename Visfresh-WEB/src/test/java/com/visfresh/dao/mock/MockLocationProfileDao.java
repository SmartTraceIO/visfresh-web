/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockLocationProfileDao extends MockDaoBase<LocationProfile, Long> implements LocationProfileDao {
    /**
     * Default constructor.
     */
    public MockLocationProfileDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.LocationProfileDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<LocationProfile> findByCompany(final Company company) {
        final List<LocationProfile> list = new LinkedList<LocationProfile>();
        for (final LocationProfile d : entities.values()) {
            if (d.getCompany().getId().equals(company.getId())) {
                list.add(d);
            }
        }
        return list;
    }
}
