/**
 *
 */
package au.smarttrace.ctrl.runner;

import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ServiceUrlHolder {
    private String serviceUrl;

    /**
     * Default constructor.
     */
    public ServiceUrlHolder() {
        super();
    }

    /**
     * @return the serviceUrl
     */
    public String getServiceUrl() {
        return serviceUrl;
    }
    /**
     * @param serviceUrl the serviceUrl to set
     */
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
