package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import com.icthh.xm.ms.balance.service.mapper.PocketMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Pocket.
 */
@Service
@Transactional
public class PocketService {

    private final PocketRepository pocketRepository;
    private final PermittedRepository permittedRepository;

    private final PocketMapper pocketMapper;

    public PocketService(PocketRepository pocketRepository,
                         PocketMapper pocketMapper,
                         PermittedRepository permittedRepository) {
        this.pocketRepository = pocketRepository;
        this.pocketMapper = pocketMapper;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a pocket.
     *
     * @param pocketDTO the entity to save
     * @return the persisted entity
     */
    public PocketDTO save(PocketDTO pocketDTO) {
        Pocket pocket = pocketMapper.toEntity(pocketDTO);
        pocket = pocketRepository.save(pocket);
        return pocketMapper.toDto(pocket);
    }

    /**
     * Get all the pockets.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<PocketDTO> findAll(String privilegeKey) {
        return permittedRepository.findAll(Pocket.class, privilegeKey).stream()
            .map(pocketMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one pocket by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PocketDTO findOne(Long id) {
        Pocket pocket = pocketRepository.findOne(id);
        return pocketMapper.toDto(pocket);
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
