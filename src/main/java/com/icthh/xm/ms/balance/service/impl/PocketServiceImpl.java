package com.icthh.xm.ms.balance.service.impl;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.PocketService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Pocket.
 */
@Service
@Transactional
public class PocketServiceImpl implements PocketService {

    private final PocketRepository pocketRepository;
    private final PermittedRepository permittedRepository;

    public PocketServiceImpl(
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
    @Override
    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    /**
     *  Get all the pockets.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pocket> findAll(String privilegeKey) {
        return permittedRepository.findAll(Pocket.class, privilegeKey);
    }

    /**
     *  Get one pocket by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Pocket findOne(Long id) {
        return pocketRepository.findOne(id);
    }

    /**
     *  Delete the  pocket by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        pocketRepository.delete(id);
    }
}
