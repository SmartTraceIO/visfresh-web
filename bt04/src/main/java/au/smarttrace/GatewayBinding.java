/**
 *
 */
package au.smarttrace;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GatewayBinding {
    private Long id;
    private Long company;
    private String gateway;
    private boolean active;

    /**
     * Default constructor.
     */
    public GatewayBinding() {
        super();
    }

    /**
     * @return the id
     */
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
     * @return the company
     */
    public Long getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Long company) {
        this.company = company;
    }
    /**
     * @return the beacon
     */
    public String getGateway() {
        return gateway;
    }
    /**
     * @param imei the IMEI to set
     */
    public void setGateway(final String imei) {
        this.gateway = imei;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
}
