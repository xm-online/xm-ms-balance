package com.icthh.xm.ms.balance.service.impl;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.service.BalanceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Balance.
 */
@Service
@Transactional
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;
    private final PermittedRepository permittedRepository;

    public BalanceServiceImpl(
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
    @Override
    public Balance save(Balance balance) {
        return balanceRepository.save(balance);
    }

    /**
     *  Get all the balances.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    @FindWithPermission("BALANCE.GET_LIST")
    public List<Balance> findAll(String privilegeKey) {
        return permittedRepository.findAll(Balance.class, privilegeKey);
    }

    /**
     *  Get one balance by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Balance findOne(Long id) {
        return balanceRepository.findOne(id);
    }

    /**
     *  Delete the  balance by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        balanceRepository.delete(id);
    }
}
