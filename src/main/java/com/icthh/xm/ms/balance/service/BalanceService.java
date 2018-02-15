package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Balance.
 */
@Service
@Transactional
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final PermittedRepository permittedRepository;

    public BalanceService(
                    BalanceRepository balanceRepository,
                    PermittedRepository permittedRepository) {
        this.balanceRepository = balanceRepository;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a balance.
     *
     * @param balance the entity to save
     * @return the persisted entity
     */
    public Balance save(Balance balance) {
        return balanceRepository.save(balance);
    }

    /**
     * Get all the balances.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("BALANCE.GET_LIST")
    public List<Balance> findAll(String privilegeKey) {
        return permittedRepository.findAll(Balance.class, privilegeKey);
    }

    /**
     * Get one balance by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Balance findOne(Long id) {
        return balanceRepository.findOne(id);
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
