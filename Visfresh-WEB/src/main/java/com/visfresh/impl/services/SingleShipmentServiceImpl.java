/**
 *
 */
package com.visfresh.impl.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.PreliminarySingleShipmentData;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.impl.singleshipment.MainShipmentDataBuilder;
import com.visfresh.impl.singleshipment.ReadingsDataBuilder;
import com.visfresh.impl.singleshipment.SingleShipmentBuildContext;
import com.visfresh.impl.singleshipment.SingleShipmentBuilderExecutor;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.services.LocationService;
import com.visfresh.services.SingleShipmentService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentServiceImpl implements SingleShipmentService {
    private static final Logger log = LoggerFactory.getLogger(SingleShipmentServiceImpl.class);
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    /**
     * Shipment dao.
     */
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private LocationService locationService;
    /**
     * Default constructor.
     */
    public SingleShipmentServiceImpl() {
        super();
    }


    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#getShipmentData(java.lang.Long)
     */
    @Override
    public SingleShipmentData getShipmentData(final long shipmentId) {
        final long startTime = System.currentTimeMillis();
        final PreliminarySingleShipmentData pd = shipmentDao.getPreliminarySingleShipmentData(
                shipmentId, null, null);
        final SingleShipmentData data = getShipmentData(pd);

        log.debug("Get SingleShipment data execution time: " + (System.currentTimeMillis() - startTime));
        return data;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#getShipmentData(java.lang.String, java.lang.Integer)
     */
    @Override
    public SingleShipmentData getShipmentData(final String sn, final int tripCount) {
        final long startTime = System.currentTimeMillis();
        final PreliminarySingleShipmentData pd = shipmentDao.getPreliminarySingleShipmentData(
                null, sn, tripCount);
        final SingleShipmentData data = getShipmentData(pd);

        log.debug("Get SingleShipment data execution time: " + (System.currentTimeMillis() - startTime));
        return data;
    }


    /**
     * @param pd preliminary data.
     * @return single shipment data.
     */
    private SingleShipmentData getShipmentData(final PreliminarySingleShipmentData pd) {
        try {
            //create executor
            final SingleShipmentBuilderExecutor executor = new SingleShipmentBuilderExecutor();
            executor.addBuilder(new MainShipmentDataBuilder(jdbc, pd.getShipment(), pd.getCompany(),
                    pd.getSiblings()));
            executor.addBuilder(new ReadingsDataBuilder(jdbc, pd.getShipment(), pd.getSiblings()));

            //launch executor
            final SingleShipmentBuildContext context = new SingleShipmentBuildContext(locationService);
            return executor.execute(context);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
