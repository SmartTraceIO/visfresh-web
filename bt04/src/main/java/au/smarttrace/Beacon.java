/**
 *
 */
package au.smarttrace;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Beacon {
    /**
     * Device IMEI code
     */
    private String imei;
    /**
     * Is active flag.
     */
    private boolean isActive;
    /**
     * Company ID.
     */
    private Long company;

    /**
     * Bound gateway if bound, null otherwise.
     */
    private GatewayBinding gateway;

    /**
     * Default constructor.
     */
    public Beacon() {
        super();
    }

    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }
    /**
     * @param isActive the isActive to set
     */
    public void setActive(final boolean isActive) {
        this.isActive = isActive;
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
     * @param gateway the gateway to set
     */
    public void setGateway(final GatewayBinding gateway) {
        this.gateway = gateway;
    }
    /**
     * @return the gateway
     */
    public GatewayBinding getGateway() {
        return gateway;
    }
}
