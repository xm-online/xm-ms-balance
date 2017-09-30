package com.icthh.xm.ms.balance.service.impl;

import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Balance.
 */
@Service
@Transactional
public class BalanceServiceImpl implements BalanceService{

    private final Logger log = LoggerFactory.getLogger(BalanceServiceImpl.class);

    private final BalanceRepository balanceRepository;

    public BalanceServiceImpl(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    /**
     * Save a balance.
     *
     * @param balance the entity to save
     * @return the persisted entity
     */
    @Override
    public Balance save(Balance balance) {
        log.debug("Request to save Balance : {}", balance);
        return balanceRepository.save(balance);
    }

    /**
     *  Get all the balances.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Balance> findAll() {
        log.debug("Request to get all Balances");
        return balanceRepository.findAll();
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
        log.debug("Request to get Balance : {}", id);
        return balanceRepository.findOne(id);
    }

    /**
     *  Delete the  balance by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Balance : {}", id);
        balanceRepository.delete(id);
    }
}
