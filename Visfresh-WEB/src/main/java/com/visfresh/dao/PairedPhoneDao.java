/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.PairedPhone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface PairedPhoneDao extends DaoBase<PairedPhone, PairedPhone, Long> {
    /**
     * @param company
     * @param filter
     * @return
     */
    int getEntityCount(Long company, Filter filter);
    /**
     * @param company
     * @param sorting
     * @param page
     * @param filter
     * @return
     */
    List<PairedPhone> findByCompany(Long company, Sorting sorting, Page page, Filter filter);
    /**
     * @param phone phone.
     * @return paired phones
     */
    List<PairedPhone> getPairedBeacons(String phone);
}
