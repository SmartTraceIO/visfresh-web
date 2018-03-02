/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface EntityWithCompany {
    /**
     * @return the company
     */
    Long getCompanyId();
    void setCompany(Long c);
}
