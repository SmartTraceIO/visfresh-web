/**
 *
 */
package com.visfresh.services.lists;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListUserItem {
    private String login;
    private String fullName;

    /**
     * Default constructor.
     */
    public ListUserItem() {
        super();
    }
    /**
     * @param u
     */
    public ListUserItem(final User u) {
        setLogin(u.getLogin());
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
    public String getLogin() {
        return login;
    }
    /**
     * @param login the login to set
     */
    public void setLogin(final String login) {
        this.login = login;
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
