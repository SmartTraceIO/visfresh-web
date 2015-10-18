/**
 *
 */
package com.visfresh.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@DiscriminatorValue(value = "ordinary")
public class Alert extends AbstractAlert {
    /**
     * Default constructor.
     */
    public Alert() {
        super();
    }
}
