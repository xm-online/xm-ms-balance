package com.icthh.xm.ms.balance.service;

import static java.math.BigDecimal.ZERO;
import static java.util.UUID.randomUUID;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.dto.PocketCharging;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Service Implementation for managing Balance.
 */
@Slf4j
@Service
@LepService(group = "service")
@Transactional
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final PermittedRepository permittedRepository;
    private final PocketRepository pocketRepository;
    private final BalanceMapper balanceMapper;
    private final ApplicationProperties applicationProperties;
    private final MetricService metricService;

    /**
     * Save a balance.
     *
     * @param balanceDTO the entity to save
     * @return the persisted entity
     */
    @LogicExtensionPoint("Save")
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
        Page<Balance> page = permittedRepository.findAll(pageable, Balance.class, privilegeKey);
        List<BalanceDTO> dtos = page.map(balanceMapper::toDto).getContent();
        Map<Long, BigDecimal> balancesAmount = balanceRepository.getBalancesAmountMap(page.getContent());
        dtos.forEach(it -> it.setAmount(balancesAmount.getOrDefault(it.getId(), ZERO)));
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
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
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);
        if (balanceDTO != null) {
            balanceDTO.setAmount(balanceRepository.findBalanceAmount(balance).orElse(ZERO));
        }
        return balanceDTO;
    }

    /**
     * Delete the balance by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        balanceRepository.delete(id);
    }

    @Transactional
    public void reload(ReloadBalanceRequest reloadRequest) {
        log.info("Start reload balance with request {}", reloadRequest);
        Balance balance = getBalanceForUpdate(reloadRequest.getBalanceId());
        reloadPocket(reloadRequest, balance);

        metricService.updateMaxMetric(balance);
    }




    private Balance getBalanceForUpdate(Long balanceId) {
        Balance balance = balanceRepository.findOneByIdForUpdate(balanceId)
            .orElseThrow(() -> new EntityNotFoundException("Balance with id " + balanceId + "not found"));

        log.debug("Found balance {}", balance);
        return balance;
    }

    private void reloadPocket(ReloadBalanceRequest reloadRequest, Balance balance) {
        Pocket pocket = findPocketForReload(reloadRequest, balance)
            .map(Pocket::getId)
            .flatMap(pocketRepository::findOneByIdForUpdate)
            .map(existingPocket -> existingPocket.addAmount(reloadRequest.getAmount()))
            .orElse(new Pocket()
                .key(randomUUID().toString())
                .balance(balance)
                .amount(reloadRequest.getAmount())
                .endDateTime(reloadRequest.getEndDateTime())
                .startDateTime(reloadRequest.getStartDateTime())
                .label(reloadRequest.getLabel())
            );


        Pocket savedPocket = pocketRepository.save(pocket);
        log.info("Pocket affected by reload {}", savedPocket);
    }

    private Optional<Pocket> findPocketForReload(ReloadBalanceRequest reloadRequest, Balance balance) {

        log.debug("Search pocket for reload balance with request {}", reloadRequest);

        Optional<Pocket> pocket = pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance(reloadRequest.getLabel(),
            reloadRequest.getStartDateTime(), reloadRequest.getEndDateTime(), balance);

        if (pocket.isPresent()) {
            log.info("Found pocket {} for reload", pocket);
        } else {
            log.info("Pocket for reload not found. New pocket will be created");
        }
        return pocket;
    }

    @Transactional
    public void charging(ChargingBalanceRequest chargingRequest) {
        log.info("Start charging balance with request {}", chargingRequest);
        Balance balance = getBalanceForUpdate(chargingRequest.getBalanceId());
        assertBalanceAmout(balance, chargingRequest.getAmount());
        chargingPockets(balance, chargingRequest.getAmount());
    }

    @Transactional
    public void transfer(TransferBalanceRequest transferRequest) {
        Long targetBalanceId = transferRequest.getTargetBalanceId();

        log.info("Start transfer balance with request {}", transferRequest);
        Balance sourceBalance = getBalanceForUpdate(transferRequest.getSourceBalanceId());
        assertBalanceAmout(sourceBalance, transferRequest.getAmount());

        List<PocketCharging> pockets = chargingPockets(sourceBalance, transferRequest.getAmount());

        Balance targetBalance = getBalanceForUpdate(targetBalanceId);
        pockets.stream().map(pocketCharging -> toReloadRequest(pocketCharging, targetBalanceId))
            .forEach(reloadRequest -> reloadPocket(reloadRequest, targetBalance));

        metricService.updateMaxMetric(targetBalance);
    }

    private ReloadBalanceRequest toReloadRequest(PocketCharging pocket, Long targetBalanceId) {
        return new ReloadBalanceRequest()
            .setAmount(pocket.getAmount())
            .setBalanceId(targetBalanceId)
            .setAmount(pocket.getAmount())
            .setEndDateTime(pocket.getEndDateTime())
            .setStartDateTime(pocket.getStartDateTime())
            .setLabel(pocket.getLabel());
    }


    private List<PocketCharging> chargingPockets(Balance balance, BigDecimal amount) {
        BigDecimal amountToCheckout = amount;
        List<PocketCharging> affectedPockets = new ArrayList<>();
        Integer pocketCheckoutBatchSize = applicationProperties.getPocketChargingBatchSize();

        for (int i = 0; amountToCheckout.compareTo(ZERO) > 0; i++) {
            PageRequest pageable = new PageRequest(i, pocketCheckoutBatchSize);
            Page<Pocket> pocketsPage = pocketRepository.findPocketForCheckoutOrderByDates(balance, pageable);
            List<Pocket> pockets = pocketsPage.getContent();
            log.debug("Fetch pockets {} by {}", pockets, pageable);
            assertNotEmpty(balance, amount, amountToCheckout, pockets);

            amountToCheckout = chargingPockets(amountToCheckout, affectedPockets, pockets);
        }

        return affectedPockets;
    }

    private BigDecimal chargingPockets(BigDecimal amountToBalanceCheckout, List<PocketCharging> affectedPockets, List<Pocket> pockets) {

        for(Pocket pocket: pockets) {
            BigDecimal pocketAmount = pocket.getAmount();
            BigDecimal amountToPocketCheckout = amountToBalanceCheckout.min(pocketAmount);
            pocket.subtractAmount(amountToPocketCheckout);
            amountToBalanceCheckout = amountToBalanceCheckout.subtract(amountToPocketCheckout);
            affectedPockets.add(new PocketCharging(pocket, amountToPocketCheckout));


            pocketRepository.save(pocket);

            log.info("Checkout pocket id:{} label:{}, from {} -> {} | leftCheckoutAmound: {}",
                pocket.getId(), pocket.getLabel(), pocketAmount, pocket.getAmount(), amountToBalanceCheckout);

            if (amountToBalanceCheckout.compareTo(ZERO) <= 0) {
                log.info("Checkout pockets finished.");
                break;
            }
        }
        return amountToBalanceCheckout;
    }

    private void assertNotEmpty(Balance balance, BigDecimal amount, BigDecimal amountToCheckout, List<Pocket> pockets) {
        if (pockets.isEmpty()) {
            throw new NoEnoughMoneyException(balance.getId(), amount.subtract(amountToCheckout));
        }
    }

    private void assertBalanceAmout(Balance balance, BigDecimal amount) {
        BigDecimal currentAmount = balanceRepository.findBalanceAmount(balance).orElse(ZERO);
        if (currentAmount.compareTo(amount) < 0) {
            throw new NoEnoughMoneyException(balance.getId(), currentAmount);
        }
    }
}
