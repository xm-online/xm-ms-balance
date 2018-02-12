package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.domain.Pocket;
import java.util.List;

/**
 * Service Interface for managing Pocket.
 */
public interface PocketService {

    /**
     * Save a pocket.
     *
     * @param pocket the entity to save
     * @return the persisted entity
     */
    Pocket save(Pocket pocket);

    /**
     *  Get all the pockets.
     *
     *  @return the list of entities
     */
    List<Pocket> findAll(String privilegeKey);

    /**
     *  Get the "id" pocket.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Pocket findOne(Long id);

    /**
     *  Delete the "id" pocket.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
