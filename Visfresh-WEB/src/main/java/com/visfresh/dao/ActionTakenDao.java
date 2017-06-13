/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.List;

import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ActionTakenDao extends DaoBase<ActionTakenView, ActionTaken, Long> {
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    ActionTakenView findOne(final Long id);
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll(java.util.Collection)
     */
    @Override
    List<ActionTakenView> findAll(Collection<Long> ids);
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll(com.visfresh.dao.Filter, com.visfresh.dao.Sorting, com.visfresh.dao.Page)
     */
    @Override
    List<ActionTakenView> findAll(Filter filter, Sorting sorting, Page page);
    /**
     * @param id action taken ID.
     * @param company company ID.
     * @return action taken view.
     */
    ActionTakenView findOne(Long id, Company company);
    /**
     * @param shipment shipment.
     * @return list of action taken view.
     */
    List<ActionTakenView> findByShipment(Shipment shipment);
}
