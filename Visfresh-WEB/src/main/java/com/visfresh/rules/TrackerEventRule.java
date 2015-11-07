/**
 *
 */
package com.visfresh.rules;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TrackerEventRule {
    /**
     * @param context tracker event context.
     * @return true if can process event.
     */
    boolean accept(RuleContext context);
    /**
     * @param context tracker event context.
     * @return true if need reprocess event.
     */
    boolean handle(RuleContext context);
}
