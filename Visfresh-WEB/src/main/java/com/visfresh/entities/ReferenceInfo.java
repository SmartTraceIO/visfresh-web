/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReferenceInfo {
    private Object id;
    private String type;

    /**
     *
     */
    public ReferenceInfo() {
        super();
    }

    /**
     * @return the id
     */
    public Object getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Object id) {
        this.id = id;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }
}
