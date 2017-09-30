package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.domain.Balance;
import java.util.List;

/**
 * Service Interface for managing Balance.
 */
public interface BalanceService {

    /**
     * Save a balance.
     *
     * @param balance the entity to save
     * @return the persisted entity
     */
    Balance save(Balance balance);

    /**
     *  Get all the balances.
     *
     *  @return the list of entities
     */
    List<Balance> findAll();

    /**
     *  Get the "id" balance.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Balance findOne(Long id);

    /**
     *  Delete the "id" balance.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
