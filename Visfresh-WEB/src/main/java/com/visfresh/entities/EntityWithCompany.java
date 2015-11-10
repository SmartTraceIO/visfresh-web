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
    Company getCompany();
    void setCompany(Company c);
}
