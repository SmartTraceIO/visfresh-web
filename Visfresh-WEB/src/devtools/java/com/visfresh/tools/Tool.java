/**
 *
 */
package com.visfresh.tools;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.init.prod.ProductionConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tool {
    /**
     * Context.
     */
    private AnnotationConfigApplicationContext context;

    /**
     *
     */
    public Tool() {
        super();
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.scan(ProductionConfig.class.getPackage().getName());
        ctxt.refresh();
        this.context = ctxt;
    }

}
