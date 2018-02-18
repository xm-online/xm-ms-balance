package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Balance.
 */
@Service
@Transactional
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final PermittedRepository permittedRepository;

    private final BalanceMapper balanceMapper;

    public BalanceService(BalanceRepository balanceRepository,
                          BalanceMapper balanceMapper,
                          PermittedRepository permittedRepository) {
        this.balanceRepository = balanceRepository;
        this.balanceMapper = balanceMapper;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a balance.
     *
     * @param balanceDTO the entity to save
     * @return the persisted entity
     */
    public BalanceDTO save(BalanceDTO balanceDTO) {
        Balance balance = balanceMapper.toEntity(balanceDTO);
        balance = balanceRepository.save(balance);
        return balanceMapper.toDto(balance);
    }

    /**
     * Get all the balances.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @FindWithPermission("BALANCE.GET_LIST")
    @Transactional(readOnly = true)
    public Page<BalanceDTO> findAll(Pageable pageable, String privilegeKey) {
        return permittedRepository.findAll(pageable, Balance.class, privilegeKey)
            .map(balanceMapper::toDto);
    }

    /**
     * Get one balance by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BalanceDTO findOne(Long id) {
        Balance balance = balanceRepository.findOne(id);
        return balanceMapper.toDto(balance);
    }

    /**
     * Delete the balance by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        balanceRepository.delete(id);
    }
}
