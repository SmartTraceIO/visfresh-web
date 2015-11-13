/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface CompanyResolver {
    Company getCompany(Long id);
}
