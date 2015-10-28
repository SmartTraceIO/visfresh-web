/**
 *
 */
package com.visfresh.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.io.ReferenceResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ServerJsonSerializer extends EntityJSonSerializer {
    /**
     * Default constructor.
     */
    public ServerJsonSerializer() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.JSonSerializer#setReferenceResolver(com.visfresh.io.ReferenceResolver)
     */
    @Override
    @Autowired
    public void setReferenceResolver(final ReferenceResolver r) {
        super.setReferenceResolver(r);
    }
}
