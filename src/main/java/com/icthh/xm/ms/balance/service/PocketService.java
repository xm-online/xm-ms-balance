package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Pocket.
 */
@Service
@Transactional
public class PocketService {

    private final PocketRepository pocketRepository;
    private final PermittedRepository permittedRepository;

    public PocketService(
                    PocketRepository pocketRepository,
                    PermittedRepository permittedRepository) {
        this.pocketRepository = pocketRepository;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a pocket.
     *
     * @param pocket the entity to save
     * @return the persisted entity
     */
    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    /**
     * Get all the pockets.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Pocket> findAll(String privilegeKey) {
        return permittedRepository.findAll(Pocket.class, privilegeKey);
    }

    /**
     * Get one pocket by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Pocket findOne(Long id) {
        return pocketRepository.findOne(id);
    }

    /**
     * Delete the pocket by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        pocketRepository.delete(id);
    }
}
