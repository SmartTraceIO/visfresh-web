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
    private String positionCompany;

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
            setFullName(buildFullName(u));
        }
        setPositionCompany(buildPositionCompany(u));
    }
    /**
     * @param u the user.
     * @return position company.
     */
    public static String buildPositionCompany(final User u) {
        final StringBuilder sb = new StringBuilder();
        sb.append(u.getPosition());
        sb.append(" - ");
        if (u.isExternal()) {
            sb.append(u.getExternalCompany());
        } else {
            sb.append(u.getCompany().getName());
        }
        return sb.toString();
    }
    /**
     * @param u the user.
     * @return full user name.
     */
    public static String buildFullName(final User u) {
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
        return sb.toString();
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
    /**
     * @param positionCompany the positionCompany to set
     */
    public void setPositionCompany(final String positionCompany) {
        this.positionCompany = positionCompany;
    }
    /**
     * @return the positionCompany
     */
    public String getPositionCompany() {
        return positionCompany;
    }
}
