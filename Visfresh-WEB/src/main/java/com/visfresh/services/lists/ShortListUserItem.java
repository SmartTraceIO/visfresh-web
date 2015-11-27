/**
 *
 */
package com.visfresh.services.lists;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortListUserItem {
    private Long id;
    private String fullName;

    /**
     * Default constructor.
     */
    public ShortListUserItem() {
        super();
    }
    /**
     * @param u
     */
    public ShortListUserItem(final User u) {
        setId(u.getId());
        if (u.getFirstName() != null || u.getLastName() != null) {
            final StringBuilder sb = new StringBuilder();
            if (u.getFirstName() != null) {
                sb.append(u.getFirstName());
            }
            if (u.getLastName() != null) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(u.getLastName());
            }
            setFullName(sb.toString());
        }
    }

    /**
     * @return the login
     */
    public Long getId() {
        return id;
    }
    /**
     * @param login the login to set
     */
    public void setId(final Long login) {
        this.id = login;
    }
    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * @param fullName the fullName to set
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }
}
