/**
 *
 */
package com.visfresh.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Device;
import com.visfresh.io.JSonFactory;
import com.visfresh.io.ReferenceResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ServerJSonFactory extends JSonFactory {
    private static final Logger log = LoggerFactory.getLogger(ServerJSonFactory.class);

    /**
     * Default constructor.
     */
    public ServerJSonFactory() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.JSonFactory#setReferenceResolver(com.visfresh.controllers.ReferenceResolver)
     */
    @Override
    @Autowired
    public void setReferenceResolver(final ReferenceResolver referenceResolver) {
        super.setReferenceResolver(referenceResolver);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.JSonFactory#resolveDevice(java.lang.String)
     */
    @Override
    protected Device resolveDevice(final String id) {
        final Device device = super.resolveDevice(id);
        if (device == null) {
            log.warn("Failed to resolve device: " + id);
        }
        return device;
    }
}
