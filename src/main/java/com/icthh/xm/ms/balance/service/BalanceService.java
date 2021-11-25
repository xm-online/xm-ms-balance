package com.icthh.xm.ms.balance.service;

import static com.icthh.xm.ms.balance.service.OperationType.CHARGING;
import static com.icthh.xm.ms.balance.service.OperationType.RELOAD;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_FROM;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_TO;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.*;
import com.icthh.xm.ms.balance.domain.BalanceSpec.AllowNegative;
import com.icthh.xm.ms.balance.domain.BalanceSpec.BalanceTypeSpec;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.dto.PocketCharging;
import com.icthh.xm.ms.balance.service.dto.TransferDto;
import com.icthh.xm.ms.balance.service.mapper.BalanceChangeEventMapper;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import com.icthh.xm.ms.balance.service.mapper.PocketChangeEventMapper;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Balance.
 */
@Slf4j
@Service
@LepService(group = "service")
@Transactional
@RequiredArgsConstructor
public class BalanceService {

    public static final String NEGATIVE_POCKET_LABEL = "NEGATIVE_POCKET";

    private final BalanceRepository balanceRepository;
    private final PermittedRepository permittedRepository;
    private final PocketRepository pocketRepository;
    private final BalanceMapper balanceMapper;
    private final BalanceChangeEventMapper balanceChangeEventMapper;
    private final ApplicationProperties applicationProperties;
    private final MetricService metricService;
    private final XmAuthenticationContextHolder authContextHolder;
    private final BalanceChangeEventRepository balanceChangeEventRepository;
    private final BalanceSpecService balanceSpecService;
    private final PocketChangeEventMapper pocketChangeEventMapper;
    @Setter(onMethod = @__(@Autowired))
    private BalanceService self;
    private Clock clock = Clock.systemDefaultZone();


    /**
     * Save a balance.
     *
     * @param balanceDTO the entity to save
     * @return the persisted entity
     */
    @LogicExtensionPoint(value = "Save", resolver = BalanceDtoTypeKeyResolver.class)
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
    @PrivilegeDescription("Privilege to get all balances")
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
    public BalanceDTO findOne(Long id, Instant applyDate) {
        Instant now = applyDate == null ? now(clock) : applyDate;
        Balance balance = balanceRepository.findById(id).orElse(null);
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);
        if (balanceDTO != null) {
            balanceDTO.setAmount(balanceRepository.findBalanceAmount(balance, now).orElse(ZERO));
        }
        return balanceDTO;
    }

    /**
     * Delete the balance by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        balanceRepository.deleteById(id);
    }

    private Balance getBalanceForUpdate(Long balanceId) {
        Balance balance = balanceRepository.findOneByIdForUpdate(balanceId)
            .orElseThrow(() -> new EntityNotFoundException("Balance with id " + balanceId + "not found"));

        log.debug("Found balance {}", balance);
        return balance;
    }

    private void reloadPocket(ReloadBalanceRequest reloadRequest, Balance balance, BalanceChangeEvent changeEvent) {
        Optional<Pocket> optionalPocket = findPocketForReload(reloadRequest, balance)
            .map(Pocket::getId)
            .flatMap(pocketRepository::findOneByIdForUpdate);

        BigDecimal amountBefore = optionalPocket.isPresent() ? optionalPocket.get().getAmount() : ZERO;

        Pocket pocket = optionalPocket
            .map(existingPocket -> existingPocket.addAmount(reloadRequest.getAmount()))
            .orElse(new Pocket()
                .key(randomUUID().toString())
                .balance(balance)
                .amount(reloadRequest.getAmount())
                .endDateTime(reloadRequest.getEndDateTime())
                .startDateTime(reloadRequest.getStartDateTime())
                .label(reloadRequest.getLabel())
                .metadata(Metadata.of(reloadRequest.getMetadata()))
            );

        Pocket savedPocket = pocketRepository.save(pocket);
        PocketChangeEvent event = createPocketChangeEvent(savedPocket, reloadRequest.getAmount(), amountBefore);
        changeEvent.addPocketChangeEvent(event);
        log.info("Pocket affected by reload {}", savedPocket);
    }

    private Optional<Pocket> findPocketForReload(ReloadBalanceRequest reloadRequest, Balance balance) {

        log.debug("Search pocket for reload balance with request {}", reloadRequest);

        Optional<Pocket> pocket = pocketRepository.findPocketForReload(
            reloadRequest.getLabel(), reloadRequest.getStartDateTime(), reloadRequest.getEndDateTime(), balance,
            new Metadata(reloadRequest.getMetadata()).getValue());

        if (pocket.isPresent()) {
            log.info("Found pocket {} for reload", pocket);
        } else {
            log.info("Pocket for reload not found. New pocket will be created");
        }
        return pocket;
    }

    @Transactional
    public BalanceChangeEventDto reload(ReloadBalanceRequest reloadRequest) {
        log.info("Start reload balance with request {}", reloadRequest);
        Balance balance = getBalanceForUpdate(reloadRequest.getBalanceId());
        return reload(balance, reloadRequest);
    }

    @Transactional
    @LogicExtensionPoint(value = "Reload", resolver = BalanceTypeKeyResolver.class)
    public BalanceChangeEventDto reload(Balance balance, ReloadBalanceRequest reloadRequest) {

        String operationUuid = reloadRequest.getUuid();
        List<BalanceChangeEvent> existBalanceChangeEvents = getBalanceChangeEventsByOperationId(operationUuid);
        BalanceChangeEventDto balanceChangeEventDto = returnExistBalanceChangeEventDto(existBalanceChangeEvents);
        if (balanceChangeEventDto != null) {
            return balanceChangeEventDto;
        }
        operationUuid = StringUtils.isNotBlank(operationUuid) ? operationUuid : UUID.randomUUID().toString();

        Instant operationDate = reloadRequest.getStartDateTime() != null ? reloadRequest.getStartDateTime() : now(clock);
        BalanceChangeEvent changeEvent = createBalanceChangeEvent(operationUuid, reloadRequest.getAmount(),
            Metadata.of(reloadRequest.getMetadata()), operationDate, balance, false, RELOAD);
        reloadPocket(reloadRequest, balance, changeEvent);

        metricService.updateMaxMetric(balance, operationDate);
        changeEvent = balanceChangeEventRepository.save(changeEvent);

        return balanceChangeEventMapper.toDto(changeEvent);
    }

    @Transactional
    public BalanceChangeEventDto charging(ChargingBalanceRequest chargingRequest) {
        log.info("Start charging balance with request {}", chargingRequest);
        Balance balance = getBalanceForUpdate(chargingRequest.getBalanceId());
        return charging(balance, chargingRequest);
    }

    @Transactional
    @LogicExtensionPoint(value = "Charging", resolver = BalanceTypeKeyResolver.class)
    public BalanceChangeEventDto charging(Balance balance, ChargingBalanceRequest chargingRequest) {
        Instant applyDate = chargingRequest.getApplyDate() != null ? chargingRequest.getApplyDate() : now(clock);
        assertBalanceAmount(balance, chargingRequest.getAmount(), applyDate, chargingRequest.isChargeAsManyAsPossible());

        String operationUuid = chargingRequest.getUuid();
        List<BalanceChangeEvent> existBalanceChangeEvents = getBalanceChangeEventsByOperationId(operationUuid);
        BalanceChangeEventDto balanceChangeEventDto = returnExistBalanceChangeEventDto(existBalanceChangeEvents, chargingRequest.isWithAffectedPocketHistory());
        if (balanceChangeEventDto != null) {
            return balanceChangeEventDto;
        }
        operationUuid = StringUtils.isNotBlank(operationUuid) ? operationUuid : UUID.randomUUID().toString();
        BalanceChangeEvent changeEvent = createBalanceChangeEvent(operationUuid, chargingRequest.getAmount(),
            Metadata.of(chargingRequest.getMetadata()), applyDate, balance, true, CHARGING);
        chargingPockets(balance, chargingRequest.getAmount(), changeEvent, applyDate, chargingRequest.isChargeAsManyAsPossible());
        changeEvent = balanceChangeEventRepository.save(changeEvent);

        BalanceChangeEventDto dto = balanceChangeEventMapper.toDto(changeEvent);
        if (chargingRequest.isWithAffectedPocketHistory()){
            dto.setPocketChangeEvents(pocketChangeEventMapper.toDto(changeEvent.getPocketChangeEvents()));
        }
        return dto;
    }

    private List<BalanceChangeEvent> getBalanceChangeEventsByOperationId(String operationUuid) {
        if (StringUtils.isBlank(operationUuid)) {
            return List.of();
        }
        return balanceChangeEventRepository.findBalanceChangeEventsByOperationId(operationUuid);
    }

    private BalanceChangeEventDto returnExistBalanceChangeEventDto(List<BalanceChangeEvent> existsBalanceChangeEvents) {
        return returnExistBalanceChangeEventDto(existsBalanceChangeEvents, false);
    }

    private BalanceChangeEventDto returnExistBalanceChangeEventDto(List<BalanceChangeEvent> existsBalanceChangeEvents, boolean withAffectedPocketHistory) {
        if (!existsBalanceChangeEvents.isEmpty()) {
            BalanceChangeEvent balanceChangeEvent = existsBalanceChangeEvents.get(0);
            log.warn("find duplicate balance change events for operationId: {} - {}, operation won't be processed",
                balanceChangeEvent.getOperationId(), existsBalanceChangeEvents);
            BalanceChangeEventDto dto = balanceChangeEventMapper.toDto(balanceChangeEvent);
            if (withAffectedPocketHistory){
                dto.setPocketChangeEvents(pocketChangeEventMapper.toDto(balanceChangeEvent.getPocketChangeEvents()));
            }
            return balanceChangeEventMapper.toDto(balanceChangeEvent);
        }
        return null;
    }

    @Transactional
    @LogicExtensionPoint("Transfer")
    public TransferDto transfer(TransferBalanceRequest transferRequest) {
        String operationUuid = transferRequest.getUuid();
        List<BalanceChangeEvent> existBalanceChangeEvents = getBalanceChangeEventsByOperationId(operationUuid);
        TransferDto transferDto = returnExistTransferDto(existBalanceChangeEvents);
        if (transferDto != null) {
            return transferDto;
        }
        operationUuid = StringUtils.isNotBlank(operationUuid) ? operationUuid : UUID.randomUUID().toString();

        Long targetBalanceId = transferRequest.getTargetBalanceId();

        log.info("Start transfer balance with request {}", transferRequest);
        Balance sourceBalance = getBalanceForUpdate(transferRequest.getSourceBalanceId());
        BigDecimal amount = transferRequest.getAmount();
        Metadata metadata = Metadata.of(transferRequest.getMetadata());

        Instant applyDate = transferRequest.getApplyDate() == null ? now(clock) : transferRequest.getApplyDate();

        assertBalanceAmount(sourceBalance, amount, applyDate);

        BalanceChangeEvent eventFrom = createBalanceChangeEvent(operationUuid, amount, metadata, applyDate, sourceBalance, true, TRANSFER_FROM);
        List<PocketCharging> pockets = chargingPockets(sourceBalance, amount, eventFrom, applyDate);

        Balance targetBalance = getBalanceForUpdate(targetBalanceId);
        BalanceChangeEvent eventTo = createBalanceChangeEvent(operationUuid, amount, metadata, applyDate, targetBalance, false, TRANSFER_TO);
        pockets.stream().map(pocketCharging -> toReloadRequest(pocketCharging, targetBalanceId, metadata))
            .forEach(reloadRequest -> reloadPocket(reloadRequest, targetBalance, eventTo));

        metricService.updateMaxMetric(targetBalance, applyDate);
        balanceChangeEventRepository.save(eventFrom);
        balanceChangeEventRepository.save(eventTo);
        return TransferDto
            .builder()
            .from(balanceChangeEventMapper.toDto(eventFrom))
            .to(balanceChangeEventMapper.toDto(eventTo))
            .build();
    }

    private BalanceChangeEvent createBalanceChangeEvent(String operationUuid, BigDecimal amount, Metadata metadata,
                                                        Instant operationDate, Balance balance, boolean isSubtractAmount,
                                                        OperationType transferTo) {
        BigDecimal amountBeforeTransferTo = balanceRepository.findBalanceAmount(balance, operationDate).orElse(ZERO);
        BigDecimal amountAfterTransferTo = getAmountAfter(isSubtractAmount, amountBeforeTransferTo, amount);
        Instant prevEntryDate = balanceChangeEventRepository
            .findLastBalanceChangeEvent(balance.getId())
            .map(BalanceChangeEvent::getEntryDate)
            .orElse(Instant.EPOCH);

        BalanceChangeEvent event = new BalanceChangeEvent();
        event.setBalanceId(balance.getId());
        event.setBalanceKey(balance.getKey());
        event.setBalanceTypeKey(balance.getTypeKey());
        event.setBalanceEntityId(balance.getEntityId());
        XmAuthenticationContext context = authContextHolder.getContext();
        event.setExecutedByUserKey(context.getUserKey().orElse(context.getRequiredLogin()));
        event.setOperationType(transferTo);
        event.setAmountDelta(amount);
        event.setAmountTotal(amount);
        event.setOperationDate(operationDate);
        event.setEntryDate(Instant.now());
        event.setPrevEntryDate(prevEntryDate);
        event.setOperationId(operationUuid);
        event.setMetadata(metadata);
        event.setAmountAfter(amountAfterTransferTo);
        event.setAmountBefore(amountBeforeTransferTo);
        return event;
    }

    private TransferDto returnExistTransferDto(List<BalanceChangeEvent> existsBalanceChangeEvents) {
        if (!existsBalanceChangeEvents.isEmpty()) {
            log.warn("find duplicate balance change events for operationId: {} - {}, operation won't be processed",
                existsBalanceChangeEvents.get(0).getOperationId(), existsBalanceChangeEvents);

            TransferDto transferDto = TransferDto.builder().build();
            existsBalanceChangeEvents.forEach(balanceChangeEvent -> {
                if (TRANSFER_FROM.equals(balanceChangeEvent.getOperationType())) {
                    transferDto.setFrom(balanceChangeEventMapper.toDto(balanceChangeEvent));
                }
                if (TRANSFER_TO.equals(balanceChangeEvent.getOperationType())) {
                    transferDto.setTo(balanceChangeEventMapper.toDto(balanceChangeEvent));
                }
            });
            return transferDto;
        }
        return null;
    }

    private ReloadBalanceRequest toReloadRequest(PocketCharging pocket, Long targetBalanceId, Metadata metadata) {
        return new ReloadBalanceRequest()
            .setAmount(pocket.getAmount())
            .setBalanceId(targetBalanceId)
            .setAmount(pocket.getAmount())
            .setEndDateTime(pocket.getEndDateTime())
            .setStartDateTime(pocket.getStartDateTime())
            .setMetadata(self.mergeMetadata(pocket.getMetadata(), metadata).getMetadata())
            .setLabel(pocket.getLabel());
    }

    @LogicExtensionPoint("MergeMetadata")
    public Metadata mergeMetadata(Metadata metadata, Metadata additionalMetadata) {
        return metadata.merge(additionalMetadata);
    }

    private List<PocketCharging> chargingPockets(Balance balance, BigDecimal amount, BalanceChangeEvent changeEvent,
                                                 Instant chargeDateTime) {
        return chargingPockets(balance, amount, changeEvent, chargeDateTime, false);
    }

    private List<PocketCharging> chargingPockets(Balance balance, BigDecimal amount, BalanceChangeEvent changeEvent,
                                                 Instant chargeDateTime, boolean isChargeAsManyAsPossible) {
        BigDecimal amountToCheckout = amount;
        List<PocketCharging> affectedPockets = new ArrayList<>();
        Integer pocketCheckoutBatchSize = applicationProperties.getPocketChargingBatchSize();
        BalanceTypeSpec balanceTypeSpec = balanceSpecService.getBalanceSpec(balance.getTypeKey());
        AllowNegative allowNegative = balanceTypeSpec.getAllowNegative();

        for (int i = 0; amountToCheckout.compareTo(ZERO) > 0; i++) {
            PageRequest pageable = PageRequest.of(i, pocketCheckoutBatchSize);
            Page<Pocket> pocketsPage = self.getPocketForCharging(balance, pageable, changeEvent,
                allowNegative.isEnabled(), chargeDateTime);
            List<Pocket> pockets = pocketsPage.getContent();
            log.debug("Fetch pockets {} by {}", pockets, pageable);
            assertNotEmpty(balance, amount, amountToCheckout, pockets, isChargeAsManyAsPossible);

            amountToCheckout = chargingPockets(amountToCheckout, affectedPockets, pocketsPage, changeEvent, balanceTypeSpec, balance);

            if (isChargeAsManyAsPossible) {
                amountToCheckout = handleAsManyAsPossibleCharging(amountToCheckout, pocketsPage.isLast(), changeEvent);
            }
        }

        if (balanceTypeSpec.isRemoveZeroPockets()) {
            pocketRepository.deletePocketWithZeroAmount(balance.getId());
        }

        return affectedPockets;
    }

    private BigDecimal handleAsManyAsPossibleCharging(BigDecimal amountToCheckout, boolean isLastPocketPage,
                                                       BalanceChangeEvent changeEvent) {
        if (amountToCheckout.compareTo(ZERO) > 0 && isLastPocketPage) {
            BigDecimal chargedAmount = changeEvent.getAmountTotal().subtract(amountToCheckout);
            changeEvent.setAmountDelta(chargedAmount);
            changeEvent.setAmountAfter(ZERO);

            amountToCheckout = ZERO;
        }
        return amountToCheckout;
    }

    @LogicExtensionPoint("GetPocketForCharging")
    public Page<Pocket> getPocketForCharging(Balance balance, PageRequest pageable, BalanceChangeEvent changeEvent,
                                             Boolean isNegativeAllowed, Instant chargeDateTime) {
        if (Boolean.TRUE.equals(isNegativeAllowed)) {
            return pocketRepository.findPocketForChargingWithNegativeOrderByDates(balance, chargeDateTime, pageable);
        }

        return pocketRepository.findPocketForChargingOrderByDates(balance, chargeDateTime, pageable);
    }

    private BigDecimal chargingPockets(BigDecimal amountToBalanceCheckout, List<PocketCharging> affectedPockets,
                                       Page<Pocket> pockets, BalanceChangeEvent changeEvent, BalanceTypeSpec balanceSpec, Balance balance) {

        for (Pocket pocket : pockets) {
            BigDecimal pocketAmount = pocket.getAmount();
            if (pocketAmount.compareTo(ZERO) <= 0) {
                continue;
            }
            BigDecimal amountToPocketCheckout = amountToBalanceCheckout.min(pocketAmount);
            pocket.subtractAmount(amountToPocketCheckout);
            amountToBalanceCheckout = amountToBalanceCheckout.subtract(amountToPocketCheckout);
            affectedPockets.add(new PocketCharging(pocket, amountToPocketCheckout));

            Pocket saved = pocketRepository.save(pocket);
            changeEvent.addPocketChangeEvent(
                createPocketChangeEvent(saved, amountToPocketCheckout, pocketAmount));

            log.info("Checkout pocket id:{} label:{}, from {} -> {} | leftCheckoutAmount: {}",
                pocket.getId(), pocket.getLabel(), pocketAmount, pocket.getAmount(), amountToBalanceCheckout);

            if (amountToBalanceCheckout.compareTo(ZERO) <= 0) {
                log.info("Checkout pockets finished.");
                break;
            }
        }

        amountToBalanceCheckout = handleNegativeCharging(amountToBalanceCheckout, balanceSpec, pockets.isLast(), balance,
            affectedPockets, changeEvent);

        return amountToBalanceCheckout;
    }

    private BigDecimal handleNegativeCharging(BigDecimal amountToBalanceCheckout, BalanceTypeSpec balanceTypeSpec, boolean isLastPocketPage,
                                              Balance balance, List<PocketCharging> affectedPockets, BalanceChangeEvent changeEvent) {
        AllowNegative allowNegative = balanceTypeSpec.getAllowNegative();
        if (amountToBalanceCheckout.compareTo(ZERO) > 0 && isLastPocketPage && allowNegative.isEnabled()) {
            Pocket negativePocket = pocketRepository.findByLabelAndBalanceId(allowNegative.getLabel(),
                balance.getId()).orElse(new Pocket()
                .key(randomUUID().toString())
                .balance(balance)
                .amount(ZERO)
                .label(allowNegative.getLabel())
            );

            BigDecimal pocketAmount = negativePocket.getAmount();
            negativePocket.subtractAmount(amountToBalanceCheckout);
            affectedPockets.add(new PocketCharging(negativePocket, amountToBalanceCheckout));

            Pocket saved = pocketRepository.save(negativePocket);
            changeEvent.addPocketChangeEvent(createPocketChangeEvent(saved, amountToBalanceCheckout, pocketAmount));

            amountToBalanceCheckout = ZERO;

            log.info("Checkout pocket id:{} label:{}, from {} -> {} | leftCheckoutAmount: {}",
                saved.getId(), saved.getLabel(), pocketAmount, saved.getAmount(), amountToBalanceCheckout);
        }
        return  amountToBalanceCheckout;
    }

    private void assertNotEmpty(Balance balance, BigDecimal amount, BigDecimal amountToCheckout, List<Pocket> pockets, boolean chargeAsManyAsPossible) {
        if (pockets.isEmpty()) {
            assertIsEnoughMoney(balance, amount.subtract(amountToCheckout), chargeAsManyAsPossible);
        }
    }

    private void assertBalanceAmount(Balance balance, BigDecimal amount, Instant applyDate) {
        assertBalanceAmount(balance, amount, applyDate, false);
    }

    private void assertBalanceAmount(Balance balance, BigDecimal amount, Instant applyDate, boolean chargeAsManyAsPossible) {
        BigDecimal currentAmount = balanceRepository.findBalanceAmount(balance, applyDate).orElse(ZERO);
        if (currentAmount.compareTo(amount) < 0) {
            assertIsEnoughMoney(balance, amount, chargeAsManyAsPossible);
        }
    }

    private void assertIsEnoughMoney(Balance balance, BigDecimal currentAmount, boolean chargeAsManyAsPossible){
        BalanceTypeSpec balanceTypeSpec = balanceSpecService.getBalanceSpec(balance.getTypeKey());
        if (!chargeAsManyAsPossible && !balanceTypeSpec.getAllowNegative().isEnabled()) {
            throw new NoEnoughMoneyException(balance.getId(), currentAmount);
        }
    }

    private PocketChangeEvent createPocketChangeEvent(Pocket pocket, BigDecimal amountDelta, BigDecimal amountBefore) {
        PocketChangeEvent event = new PocketChangeEvent();
        event.setPocketId(pocket.getId());
        event.setPocketKey(pocket.getKey());
        event.setPocketLabel(pocket.getLabel());
        event.setAmountDelta(amountDelta);
        event.setMetadata(pocket.getMetadata());
        event.setAmountAfter(pocket.getAmount());
        event.setAmountBefore(amountBefore);
        return event;
    }

    private BigDecimal getAmountAfter(boolean isSubtractAmount, BigDecimal balanceAmount, BigDecimal deltaAmount) {
        return isSubtractAmount ? balanceAmount.subtract(deltaAmount) : balanceAmount.add(deltaAmount);
    }
}
