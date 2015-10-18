/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name = "companies")
public class Company implements EntityWithId {
    /**
     * Company ID.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    /**
     * Company name.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Company description
     */
    @Column
    private String description;

    /**
     * Default constructor.
     */
    public Company() {
        super();
    }

    /**
     * @param l
     */
    public Company(final long l) {
        super();
        setId(l);
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
}
