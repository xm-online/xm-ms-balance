package com.icthh.xm.ms.balance.service.impl;

import com.icthh.xm.ms.balance.service.PocketService;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Pocket.
 */
@Service
@Transactional
public class PocketServiceImpl implements PocketService{

    private final Logger log = LoggerFactory.getLogger(PocketServiceImpl.class);

    private final PocketRepository pocketRepository;

    public PocketServiceImpl(PocketRepository pocketRepository) {
        this.pocketRepository = pocketRepository;
    }

    /**
     * Save a pocket.
     *
     * @param pocket the entity to save
     * @return the persisted entity
     */
    @Override
    public Pocket save(Pocket pocket) {
        log.debug("Request to save Pocket : {}", pocket);
        return pocketRepository.save(pocket);
    }

    /**
     *  Get all the pockets.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pocket> findAll() {
        log.debug("Request to get all Pockets");
        return pocketRepository.findAll();
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
        log.debug("Request to get Pocket : {}", id);
        return pocketRepository.findOne(id);
    }

    /**
     *  Delete the  pocket by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Pocket : {}", id);
        pocketRepository.delete(id);
    }
}
