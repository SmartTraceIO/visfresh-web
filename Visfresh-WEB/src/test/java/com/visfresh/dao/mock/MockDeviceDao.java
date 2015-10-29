/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceDao extends MockDaoBase<Device, String> implements DeviceDao {

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#findAllByImei(java.lang.String)
     */
    @Override
    public List<Device> findAllByImei(final String imei) {
        final List<Device> list = new LinkedList<Device>();
        for (final Device d : entities.values()) {
            if (d.getImei().equals(imei)) {
                list.add(d);
            }
        }
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<Device> findByCompany(final Company company) {
        final List<Device> list = new LinkedList<Device>();
        for (final Device d : entities.values()) {
            if (d.getCompany().getId().equals(company.getId())) {
                list.add(d);
            }
        }
        return list;
    }
}
