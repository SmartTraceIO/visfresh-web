/**
 *
 */
package com.visfresh.reports.shipment;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportBuilderTest {

    /**
     * Default constructor.
     */
    public ShipmentReportBuilderTest() {
        super();
    }

    @Test
    public void testLoadAlertImage() {
        assertNotNull(ShipmentReportBuilder.loadAlertImages());
    }
}
