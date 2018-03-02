/**
 *
 */
package com.visfresh.lists;

import com.visfresh.entities.Company;
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
     * @param u user.
     * @param company user's company
     */
    public ShortListUserItem(final User u, final Company company) {
        setId(u.getId());
        if (u.getFirstName() != null || u.getLastName() != null) {
            setFullName(buildFullName(u));
        }
        setPositionCompany(buildPositionCompany(u, company));
    }
    /**
     * @param u the user.
     * @param company TODO
     * @return position company.
     */
    public static String buildPositionCompany(final User u, final Company company) {
        final StringBuilder sb = new StringBuilder();
        if (u.getPosition() != null) {
            sb.append(u.getPosition());
        }
        if (!u.isExternal()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(company.getName());
        } else if (u.getExternalCompany() != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(u.getExternalCompany());
        }

        return sb.toString();
    }
    /**
     * @param u the user.
     * @return full user name.
     */
    public static String buildFullName(final User u) {
        return buildFullName(u.getFirstName(), u.getLastName());
    }
    /**
     * @param firstName first name.
     * @param lastName last name.
     * @return full user name.
     */
    public static String buildFullName(final String firstName, final String lastName) {
        final StringBuilder sb = new StringBuilder();
        if (firstName != null) {
            sb.append(firstName);
        }
        if (lastName != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lastName);
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
