/**
 *
 */
package com.visfresh.reports.shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ImageRenderingInfo {
    private final String resource;
    private final boolean shouldFlip;

    /**
     * @param resource image resource.
     */
    public ImageRenderingInfo(final String resource) {
        this(resource, false);
    }
    /**
     * @param resource image resource.
     */
    public ImageRenderingInfo(final String resource, final boolean flip) {
        super();
        this.resource = resource;
        shouldFlip = flip;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }
    /**
     * @return the shouldFlip
     */
    public boolean shouldFlip() {
        return shouldFlip;
    }
}
