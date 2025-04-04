package com.icthh.xm.ms.balance.service;

import static com.icthh.xm.ms.balance.service.OperationType.CHANGE_STATUS;
import static com.icthh.xm.ms.balance.service.OperationType.CHARGING;
import static com.icthh.xm.ms.balance.service.OperationType.RELOAD;
import static com.icthh.xm.ms.balance.service.OperationType.REVERT_CHARGING;
import static com.icthh.xm.ms.balance.service.OperationType.REVERT_RELOAD;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_FROM;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_TO;
import static com.icthh.xm.ms.balance.service.RevertReloadMode.AS_MANY_AS_POSSIBLE_FROM_BALANCE;
import static com.icthh.xm.ms.balance.service.RevertReloadMode.AS_MANY_AS_POSSIBLE_FROM_POCKETS;
import static com.icthh.xm.ms.balance.service.RevertReloadMode.DEFAULT;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;

import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.BusinessNotFoundException;
import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.BalanceSpec.AllowNegative;
import com.icthh.xm.ms.balance.domain.BalanceSpec.BalanceTypeSpec;
import com.icthh.xm.ms.balance.domain.Metadata;
import com.icthh.xm.ms.balance.domain.NextSpec;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.dto.PocketChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.PocketChanging;
import com.icthh.xm.ms.balance.service.dto.TransferDto;
import com.icthh.xm.ms.balance.service.mapper.BalanceChangeEventMapper;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import com.icthh.xm.ms.balance.service.mapper.PocketChangeEventMapper;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.RevertBalanceOperationRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
        assertStatus(balance.getStatus(), balance.getTypeKey());
        balance = balanceRepository.save(balance);
        return balanceMapper.toDto(balance);
    }

    private void assertStatus(String status, String typeKey) {
        List<String> statuses = balanceSpecService.getBalanceStatusKeys(typeKey);

        if (!statuses.isEmpty() && !statuses.contains(status)) {
            throw new BusinessException(ErrorConstants.ERR_VALIDATION,
                "Unsupported status [" + status + "] for type key: " + typeKey);
        }
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

    private List<PocketChanging> reloadPocketWithNegativePockets(ReloadBalanceRequest reloadRequest, Balance balance,
                                                                 BigDecimal amount, BalanceChangeEvent changeEvent) {
        BigDecimal amountToCheckout = amount;
        List<PocketChanging> affectedPockets = new ArrayList<>();
        Integer pocketCheckoutBatchSize = applicationProperties.getPocketChargingBatchSize();

        for (int i = 0; amountToCheckout.compareTo(ZERO) > 0; i++) {
            PageRequest pageable = PageRequest.of(i, pocketCheckoutBatchSize);
            Page<Pocket> pocketsPage = pocketRepository.findPocketForReloadingWithNegativeOrderByDates(balance, pageable);
            List<Pocket> pockets = pocketsPage.getContent();
            log.debug("Fetch negative pockets for reload {} by {}", pockets, pageable);

            amountToCheckout = reloadNegativePockets(reloadRequest, balance, amountToCheckout, affectedPockets,
                pocketsPage, changeEvent);
        }
        return affectedPockets;
    }

    private BigDecimal reloadNegativePockets(ReloadBalanceRequest reloadRequest, Balance balance,
                                             BigDecimal amountToBalanceReload, List<PocketChanging> affectedPockets,
                                             Page<Pocket> pockets, BalanceChangeEvent changeEvent) {

        for (Pocket pocket : pockets) {
            BigDecimal pocketAmount = pocket.getAmount();
            if (pocketAmount.compareTo(ZERO) >= 0) {
                continue;
            }

            BigDecimal amountToPocketCheckout = amountToBalanceReload.min(pocketAmount.abs());
            pocket.addAmount(amountToPocketCheckout);
            amountToBalanceReload = amountToBalanceReload.subtract(amountToPocketCheckout);
            affectedPockets.add(new PocketChanging(pocket, amountToPocketCheckout));

            Pocket saved = pocketRepository.save(pocket);
            changeEvent.addPocketChangeEvent(createPocketChangeEvent(saved, amountToPocketCheckout, pocketAmount));

            log.info("Reload pocket id:{} label:{}, from {} -> {} | leftToReloadAmount: {}",
                pocket.getId(), pocket.getLabel(), pocketAmount, pocket.getAmount(), amountToBalanceReload);

            if (amountToBalanceReload.compareTo(ZERO) <= 0) {
                log.info("Checkout pockets finished.");
                break;
            }
        }

        if (pockets.isLast() && amountToBalanceReload.compareTo(ZERO) > 0) {
            reloadPocket(reloadRequest, balance, changeEvent, amountToBalanceReload);
            amountToBalanceReload = ZERO;
        }

        return amountToBalanceReload;
    }

    private void reloadPocket(ReloadBalanceRequest reloadRequest, Balance balance, BalanceChangeEvent changeEvent, @NotNull @Min(0) BigDecimal amountToReload) {
        Optional<Pocket> optionalPocket = findPocketForReload(reloadRequest, balance)
            .map(Pocket::getId)
            .flatMap(pocketRepository::findOneByIdForUpdate);

        BigDecimal amountBefore = optionalPocket.isPresent() ? optionalPocket.get().getAmount() : ZERO;

        Pocket pocket = optionalPocket
            .map(existingPocket -> existingPocket.addAmount(amountToReload))
            .orElse(new Pocket()
                .key(randomUUID().toString())
                .balance(balance)
                .amount(amountToReload)
                .endDateTime(reloadRequest.getEndDateTime())
                .startDateTime(reloadRequest.getStartDateTime())
                .label(reloadRequest.getLabel())
                .metadata(Metadata.of(reloadRequest.getMetadata()))
            );

        Pocket savedPocket = pocketRepository.save(pocket);
        PocketChangeEvent event = createPocketChangeEvent(savedPocket, amountToReload, amountBefore);
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
    public BalanceChangeEventDto revertBalanceOperation(RevertBalanceOperationRequest revertRequest) {
        log.info("Start revert balance operation with request {}", revertRequest);
        Balance balance = getBalanceForUpdate(revertRequest.getBalanceId());
        return self.revertBalanceOperation(balance, revertRequest);
    }

    @Transactional
    @LogicExtensionPoint(value = "Revert", resolver = BalanceTypeKeyResolver.class)
    public BalanceChangeEventDto revertBalanceOperation(Balance balance, RevertBalanceOperationRequest revertRequest) {
        Optional<BalanceChangeEventDto> existingRevertOperation = findExistBalanceChangeEventDto(revertRequest.getUuid(), false);
        if (existingRevertOperation.isPresent()) {
            return existingRevertOperation.get();
        }

        String uuidToRevert = revertRequest.getUuidToRevert();
        log.info("Start revert balance operation with UUID: {}", uuidToRevert);

        BalanceChangeEventDto operationToRevert = findExistBalanceChangeEventDto(uuidToRevert, true)
            .orElseThrow(() -> {
                log.error("Not found balance change event with UUID: {}", uuidToRevert);
                return new EntityNotFoundException("Balance change event with UUID " + uuidToRevert + " not found");
            });

        assertRevertOperationNotPerformed(uuidToRevert);

        OperationType previousOperationType = operationToRevert.getOperationType();
        log.info("Operation type to revert: {}", previousOperationType);

        if (previousOperationType.equals(RELOAD)) {
            return revertReloadOperation(balance, revertRequest, operationToRevert);
        } else if (previousOperationType.equals(CHARGING)) {
            return revertChargingOperation(balance, revertRequest, operationToRevert);
        } else {
            log.error("Revert of {} operation type is not supported", operationToRevert.getOperationType());
            throw new BusinessException("error.revert.operation.type.not.supported", "Revert operation type not supported");
        }
    }

    private BalanceChangeEventDto revertReloadOperation(Balance balance,
                                                        RevertBalanceOperationRequest revertRequest,
                                                        BalanceChangeEventDto reloadOperationToRevert) {
        String operationUuid = StringUtils.isNotBlank(revertRequest.getUuid())
            ? revertRequest.getUuid()
            : UUID.randomUUID().toString();

        BalanceChangeEvent changeEvent = createBalanceChangeEvent(
            operationUuid,
            reloadOperationToRevert.getAmountTotal(),
            Metadata.of(revertRequest.getMetadata()),
            now(clock),
            balance,
            revertRequest.getExecutedBy(),
            true,
            REVERT_RELOAD
        );
        changeEvent.setRevertOperationId(revertRequest.getUuidToRevert());


        RevertReloadMode revertReloadMode = revertRequest.getRevertReloadMode();
        log.info("Revert reload mode: {}", revertReloadMode);

        if (revertReloadMode.equals(DEFAULT) || revertReloadMode.equals(AS_MANY_AS_POSSIBLE_FROM_POCKETS)) {
            revertReloadByMode(revertReloadMode, reloadOperationToRevert.getPocketChangeEvents(), changeEvent);
        } else if (revertReloadMode.equals(AS_MANY_AS_POSSIBLE_FROM_BALANCE)) {
            revertReloadByAsManyAsPossibleFromBalance(balance, reloadOperationToRevert.getPocketChangeEvents(), changeEvent);
        } else {
            log.error("Could not perform operation, unknown revert reload mode: {}", revertReloadMode);
            throw new BusinessException("error.revert.reload.mode.unknown", "Unknown revert reload mode");
        }

        changeEvent = balanceChangeEventRepository.save(changeEvent);

        return balanceChangeEventMapper.toDto(changeEvent);
    }

    private List<PocketChanging> revertReloadByMode(RevertReloadMode revertReloadMode,
                                                    List<PocketChangeEventDto> pocketChangeEventDtoList,
                                                    BalanceChangeEvent changeEvent) {
        List<PocketChangeEvent> pocketChangeEvents = new ArrayList<>();
        List<PocketChanging> pocketChangingList = new ArrayList<>();

        for (PocketChangeEventDto pocketChangeEventDto : pocketChangeEventDtoList) {
            Pocket pocket = getPocketForUpdate(pocketChangeEventDto.getPocketId());

            BigDecimal amountBefore = pocket.getAmount();
            BigDecimal amountToProvide = pocketChangeEventDto.getAmountDelta();

            boolean isChargingToNegativeRemainder = amountBefore.subtract(amountToProvide).compareTo(ZERO) < 0;

            if (revertReloadMode.equals(DEFAULT) && isChargingToNegativeRemainder) {
                log.error("Could not revert reload, not enough amount in pocket");
                throw new BusinessException("error.revert.reload.insufficient.pocket.amount", "Operation provide negative remainder");
            } else if (revertReloadMode.equals(AS_MANY_AS_POSSIBLE_FROM_POCKETS) && isChargingToNegativeRemainder) {
                pocket.setAmount(ZERO);
                amountToProvide = amountBefore;
            } else {
                pocket.subtractAmount(amountToProvide);
            }

            Pocket savedPocket = pocketRepository.save(pocket);
            PocketChangeEvent pocketChangeEvent = createPocketChangeEvent(savedPocket, amountToProvide, amountBefore);

            pocketChangeEvents.add(pocketChangeEvent);
            pocketChangingList.add(new PocketChanging(savedPocket, pocketChangeEvent.getAmountDelta()));
        }

        if (revertReloadMode.equals(AS_MANY_AS_POSSIBLE_FROM_POCKETS)) {
            BigDecimal amountDelta = pocketChangeEvents.stream()
                .map(PocketChangeEvent::getAmountDelta)
                .reduce(ZERO, BigDecimal::add);

            changeEvent.setAmountDelta(amountDelta);
            changeEvent.setAmountAfter(changeEvent.getAmountBefore().subtract(amountDelta));
        }

        changeEvent.addPocketChangeEvents(pocketChangeEvents);
        return pocketChangingList;
    }

    private List<PocketChanging> revertReloadByAsManyAsPossibleFromBalance(Balance balance,
                                                                           List<PocketChangeEventDto> pocketChangeEventDtoList,
                                                                           BalanceChangeEvent changeEvent) {
        List<PocketChanging> pocketChangingList = revertReloadByMode(AS_MANY_AS_POSSIBLE_FROM_POCKETS, pocketChangeEventDtoList, changeEvent);

        if (changeEvent.getAmountTotal().compareTo(changeEvent.getAmountDelta()) > 0) {
            BigDecimal amountToChargeFromBalance = changeEvent.getAmountTotal().subtract(changeEvent.getAmountDelta());
            pocketChangingList.addAll(
                chargingPockets(balance, amountToChargeFromBalance, changeEvent, now(clock), true, null)
            );

            BigDecimal amountDelta = pocketChangingList.stream()
                .map(PocketChanging::getAmount)
                .reduce(ZERO, BigDecimal::add);

            changeEvent.setAmountDelta(amountDelta);
            changeEvent.setAmountAfter(changeEvent.getAmountBefore().subtract(amountDelta));
        }

        return pocketChangingList;
    }

    private BalanceChangeEventDto revertChargingOperation(Balance balance,
                                                          RevertBalanceOperationRequest revertRequest,
                                                          BalanceChangeEventDto chargingOperationToRevert) {
        String operationUuid = StringUtils.isNotBlank(revertRequest.getUuid())
            ? revertRequest.getUuid()
            : UUID.randomUUID().toString();

        BalanceChangeEvent changeEvent = createBalanceChangeEvent(
            operationUuid,
            chargingOperationToRevert.getAmountTotal(),
            Metadata.of(revertRequest.getMetadata()),
            now(clock),
            balance,
            revertRequest.getExecutedBy(),
            false,
            REVERT_CHARGING
        );
        changeEvent.setRevertOperationId(revertRequest.getUuidToRevert());

        boolean isNeedToReturnAmountToNewPocket = isNeedToReturnAmountToNewPocket(revertRequest.getStartDateTime(), revertRequest.getEndDateTime());
        log.info("Is need to return amount to new pocket: {}", isNeedToReturnAmountToNewPocket);
        if (isNeedToReturnAmountToNewPocket) {
            return revertToNewPocket(balance, revertRequest, chargingOperationToRevert, changeEvent);
        } else {
            return revertToOldPockets(chargingOperationToRevert, changeEvent);
        }
    }

    private BalanceChangeEventDto revertToNewPocket(Balance balance,
                                                    RevertBalanceOperationRequest revertRequest,
                                                    BalanceChangeEventDto chargingOperationToRevert,
                                                    BalanceChangeEvent changeEvent) {
        Pocket pocket = new Pocket()
            .key(randomUUID().toString())
            .balance(balance)
            .amount(chargingOperationToRevert.getAmountDelta())
            .endDateTime(revertRequest.getEndDateTime())
            .startDateTime(revertRequest.getStartDateTime())
            .label(revertRequest.getLabel())
            .metadata(Metadata.of(revertRequest.getMetadata()));

        Pocket savedPocket = pocketRepository.save(pocket);
        PocketChangeEvent event = createPocketChangeEvent(savedPocket, chargingOperationToRevert.getAmountDelta(), ZERO);

        changeEvent.addPocketChangeEvent(event);

        changeEvent = balanceChangeEventRepository.save(changeEvent);

        return balanceChangeEventMapper.toDto(changeEvent);
    }

    private BalanceChangeEventDto revertToOldPockets(BalanceChangeEventDto chargingOperationToRevert,
                                                     BalanceChangeEvent changeEvent) {
        Map<Long, Pocket> pocketIdToPocketMap = new HashMap<>();

        for (PocketChangeEventDto pocketChangeEventDto : chargingOperationToRevert.getPocketChangeEvents()) {
            Pocket pocket = getPocketForUpdate(pocketChangeEventDto.getPocketId());
            pocketIdToPocketMap.put(pocket.getId(), pocket);
        }
        for (PocketChangeEventDto pocketChangeEventDto : chargingOperationToRevert.getPocketChangeEvents()) {
            Pocket pocket = pocketIdToPocketMap.get(pocketChangeEventDto.getPocketId());

            BigDecimal amountBefore = pocket.getAmount();
            BigDecimal amountToProvide = pocketChangeEventDto.getAmountDelta();

            pocket.addAmount(amountToProvide);

            Pocket savedPocket = pocketRepository.save(pocket);
            PocketChangeEvent event = createPocketChangeEvent(savedPocket, amountToProvide, amountBefore);
            changeEvent.addPocketChangeEvent(event);
        }

        changeEvent = balanceChangeEventRepository.save(changeEvent);

        return balanceChangeEventMapper.toDto(changeEvent);
    }

    private void assertRevertOperationNotPerformed(String uuidToRevert) {
        boolean isRevertOperationAlreadyPerformed = balanceChangeEventRepository.existsByRevertOperationId(uuidToRevert);
        if (isRevertOperationAlreadyPerformed) {
            log.error("Revert operation for UUID: {} - already performed", uuidToRevert);
            throw new BusinessException("error.revert.operation.already.performed", "Revert operation already performed");
        }
    }

    private Pocket getPocketForUpdate(Long pocketId) {
        return pocketRepository.findOneByIdForUpdate(pocketId)
            .orElseThrow(() -> {
                log.error("Not found pocket with id: {}", pocketId);
                return new EntityNotFoundException("Pocket with id " + pocketId + " not found");
            });
    }

    private boolean isNeedToReturnAmountToNewPocket(Instant startDateTime, Instant endDateTime) {
        return startDateTime != null && endDateTime != null;
    }

    @Transactional
    public BalanceChangeEventDto reload(ReloadBalanceRequest reloadRequest) {
        log.info("Start reload balance with request {}", reloadRequest);
        Balance balance = getBalanceForUpdate(reloadRequest.getBalanceId());
        return self.reload(balance, reloadRequest);
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
        operationDate = reloadRequest.getOperationDate() != null ? reloadRequest.getOperationDate() : operationDate;
        BalanceChangeEvent changeEvent = createBalanceChangeEvent(operationUuid, reloadRequest.getAmount(),
            Metadata.of(reloadRequest.getMetadata()), operationDate, balance, reloadRequest.getExecutedBy(),
            false, RELOAD);
        if (reloadRequest.isReloadNegativePocket()) {
            reloadPocketWithNegativePockets(reloadRequest, balance, reloadRequest.getAmount(), changeEvent);
        } else {
            reloadPocket(reloadRequest, balance, changeEvent, reloadRequest.getAmount());
        }

        metricService.updateMaxMetric(balance, operationDate);
        changeEvent = balanceChangeEventRepository.save(changeEvent);

        return balanceChangeEventMapper.toDto(changeEvent);
    }

    @Transactional
    public BalanceChangeEventDto charging(ChargingBalanceRequest chargingRequest) {
        log.info("Start charging balance with request {}", chargingRequest);
        Balance balance = getBalanceForUpdate(chargingRequest.getBalanceId());
        return self.charging(balance, chargingRequest);
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
            Metadata.of(chargingRequest.getMetadata()), applyDate, balance, chargingRequest.getExecutedBy(),
            true, CHARGING);
        chargingPockets(balance, chargingRequest.getAmount(), changeEvent, applyDate, chargingRequest.isChargeAsManyAsPossible(), chargingRequest.getNegativePocketLabel());
        changeEvent = balanceChangeEventRepository.save(changeEvent);

        BalanceChangeEventDto dto = balanceChangeEventMapper.toDto(changeEvent);
        if (chargingRequest.isWithAffectedPocketHistory()) {
            dto.setPocketChangeEvents(pocketChangeEventMapper.toDto(changeEvent.getPocketChangeEvents()));
        }
        return dto;
    }

    private Optional<BalanceChangeEventDto> findExistBalanceChangeEventDto(String operationUuid, boolean withAffectedPocketHistory) {
        List<BalanceChangeEvent> existBalanceChangeEvents = getBalanceChangeEventsByOperationId(operationUuid);
        return Optional.ofNullable(returnExistBalanceChangeEventDto(existBalanceChangeEvents, withAffectedPocketHistory));
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
            if (withAffectedPocketHistory) {
                List<PocketChangeEvent> pocketChangeEvents = balanceChangeEvent.getPocketChangeEvents();
                Hibernate.initialize(pocketChangeEvents);
                dto.setPocketChangeEvents(pocketChangeEventMapper.toDto(pocketChangeEvents));
            }
            return dto;
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

        BalanceChangeEvent eventFrom = createBalanceChangeEvent(operationUuid, amount, metadata, applyDate,
            sourceBalance, transferRequest.getExecutedBy(), true, TRANSFER_FROM);
        List<PocketChanging> pockets = chargingPockets(sourceBalance, amount, eventFrom, applyDate);

        Balance targetBalance = getBalanceForUpdate(targetBalanceId);
        BalanceChangeEvent eventTo = createBalanceChangeEvent(operationUuid, amount, metadata, applyDate, targetBalance,
            transferRequest.getExecutedBy(), false, TRANSFER_TO);
        pockets.stream().map(pocketCharging -> toReloadRequest(pocketCharging, targetBalanceId, metadata))
            .forEach(reloadRequest -> reloadPocket(reloadRequest, targetBalance, eventTo, reloadRequest.getAmount()));

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
                                                        Instant operationDate, Balance balance,
                                                        String executedBy, boolean isSubtractAmount,
                                                        OperationType transferTo) {
        BigDecimal amountBeforeTransferTo = balanceRepository.findBalanceAmount(balance, operationDate).orElse(ZERO);
        BigDecimal amountAfterTransferTo = getAmountAfter(isSubtractAmount, amountBeforeTransferTo, amount);
        Optional<BalanceChangeEvent> lastBalanceChangeEvent = balanceChangeEventRepository
            .findLastBalanceChangeEvent(balance.getId());
        Instant prevEntryDate = lastBalanceChangeEvent
            .map(BalanceChangeEvent::getEntryDate)
            .orElse(Instant.EPOCH);

        lastBalanceChangeEvent.ifPresent(balanceChangeEvent -> {
            balanceChangeEvent.setLast(false);
            balanceChangeEventRepository.save(balanceChangeEvent);
        });

        BalanceChangeEvent event = new BalanceChangeEvent();
        event.setBalanceId(balance.getId());
        event.setBalanceKey(balance.getKey());
        event.setBalanceTypeKey(balance.getTypeKey());
        event.setBalanceEntityId(balance.getEntityId());
        XmAuthenticationContext context = authContextHolder.getContext();
        event.setExecutedByUserKey(context.getUserKey().orElse(context.getRequiredLogin()));
        event.setExecutedBy(ofNullable(executedBy).orElse(context.getRequiredLogin()));
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
        event.setLast(true);
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

    private ReloadBalanceRequest toReloadRequest(PocketChanging pocket, Long targetBalanceId, Metadata metadata) {
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

    private List<PocketChanging> chargingPockets(Balance balance, BigDecimal amount, BalanceChangeEvent changeEvent,
                                                 Instant chargeDateTime) {
        return chargingPockets(balance, amount, changeEvent, chargeDateTime, false, null);
    }

    private List<PocketChanging> chargingPockets(Balance balance, BigDecimal amount, BalanceChangeEvent changeEvent,
                                                 Instant chargeDateTime, boolean isChargeAsManyAsPossible, String negativePocketLabel) {
        BigDecimal amountToCheckout = amount;
        List<PocketChanging> affectedPockets = new ArrayList<>();
        Integer pocketCheckoutBatchSize = applicationProperties.getPocketChargingBatchSize();
        BalanceTypeSpec balanceTypeSpec = balanceSpecService.getBalanceSpec(balance.getTypeKey());

        AllowNegative allowNegative = balanceTypeSpec.getAllowNegative();
        if (StringUtils.isNotBlank(negativePocketLabel)) {
            allowNegative = new AllowNegative();
            allowNegative.setLabel(negativePocketLabel);
            allowNegative.setEnabled(balanceTypeSpec.getAllowNegative().isEnabled());
        }

        for (int i = 0; amountToCheckout.compareTo(ZERO) > 0; i++) {
            PageRequest pageable = PageRequest.of(i, pocketCheckoutBatchSize);
            Page<Pocket> pocketsPage = self.getPocketForCharging(balance, pageable, changeEvent,
                allowNegative.isEnabled(), chargeDateTime);
            List<Pocket> pockets = pocketsPage.getContent();
            log.debug("Fetch pockets {} by {}", pockets, pageable);
            assertNotEmpty(balance, amount, amountToCheckout, pockets, isChargeAsManyAsPossible);

            amountToCheckout = chargingPockets(amountToCheckout, affectedPockets, pocketsPage, changeEvent, allowNegative, balance);

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

    private BigDecimal chargingPockets(BigDecimal amountToBalanceCheckout, List<PocketChanging> affectedPockets,
                                       Page<Pocket> pockets, BalanceChangeEvent changeEvent, AllowNegative allowNegative, Balance balance) {

        for (Pocket pocket : pockets) {
            BigDecimal pocketAmount = pocket.getAmount();
            if (pocketAmount.compareTo(ZERO) <= 0) {
                continue;
            }
            BigDecimal amountToPocketCheckout = amountToBalanceCheckout.min(pocketAmount);
            pocket.subtractAmount(amountToPocketCheckout);
            amountToBalanceCheckout = amountToBalanceCheckout.subtract(amountToPocketCheckout);
            affectedPockets.add(new PocketChanging(pocket, amountToPocketCheckout));

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

        amountToBalanceCheckout = handleNegativeCharging(amountToBalanceCheckout, allowNegative, pockets.isLast(), balance,
            affectedPockets, changeEvent);

        return amountToBalanceCheckout;
    }

    private BigDecimal handleNegativeCharging(BigDecimal amountToBalanceCheckout, AllowNegative allowNegative, boolean isLastPocketPage,
                                              Balance balance, List<PocketChanging> affectedPockets, BalanceChangeEvent changeEvent) {
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
            affectedPockets.add(new PocketChanging(negativePocket, amountToBalanceCheckout));

            Pocket saved = pocketRepository.save(negativePocket);
            changeEvent.addPocketChangeEvent(createPocketChangeEvent(saved, amountToBalanceCheckout, pocketAmount));

            amountToBalanceCheckout = ZERO;

            log.info("Checkout pocket id:{} label:{}, from {} -> {} | leftCheckoutAmount: {}",
                saved.getId(), saved.getLabel(), pocketAmount, saved.getAmount(), amountToBalanceCheckout);
        }
        return amountToBalanceCheckout;
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

    private void assertIsEnoughMoney(Balance balance, BigDecimal currentAmount, boolean chargeAsManyAsPossible) {
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

    @Transactional
    public BalanceDTO updateStatus(Long id, String status, Map<String, Object> context) {
        log.info("Start update balance status: id: {}, status: {}, context: {}", id, status, context);
        Balance balance = getBalanceForUpdate(id);
        return self.updateStatus(balance, status, context);
    }

    @Transactional
    @LogicExtensionPoint(value = "ChangeStatus", resolver = BalanceTypeKeyResolver.class)
    public BalanceDTO updateStatus(Balance balance, String status, Map<String, Object> context) {
        assertStatusTransition(status, balance);
        BalanceChangeEvent changeEvent = createChangeStatusChangeEvent(balance, status);
        log.info("Update balance status changeEvent: {}", changeEvent);
        balanceChangeEventRepository.save(changeEvent);

        balance.setStatus(status);
        log.info("Update balance: {}", balance);
        balance = balanceRepository.save(balance);

        return balanceMapper.toDto(balance);
    }

    private void assertStatusTransition(String newStatus, Balance balance) {
        String currentStatus = balance.getStatus();
        String balanceTypeKey = balance.getTypeKey();

        if (currentStatus == null) {
            assertNullStatusTransition(newStatus, balanceTypeKey);
            return;
        }

        Optional<List<NextSpec>> nextSpecs = balanceSpecService.getNextStatusSpecs(balanceTypeKey, currentStatus);
        if (nextSpecs.isEmpty() || nextSpecs.get().stream().map(NextSpec::getStatusKey).noneMatch(newStatus::equals)) {
            throw new StatusTransitionException(newStatus, currentStatus, balanceTypeKey);
        }
    }

    private void assertNullStatusTransition(String newStatus, String balanceTypeKey) {
        List<String> balanceStatusKeys = balanceSpecService.getBalanceStatusKeys(balanceTypeKey);
        if (!balanceStatusKeys.contains(newStatus)) {
            throw new StatusTransitionException(newStatus, null, balanceTypeKey);
        }
    }

    private BalanceChangeEvent createChangeStatusChangeEvent(Balance balance, String status) {
        String operationUuid = UUID.randomUUID().toString();
        Instant operationDate = now(clock);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(MetadataConstants.CHANGE_STATUS_FROM, balance.getStatus());
        metadata.put(MetadataConstants.CHANGE_STATUS_TO, status);

        return createBalanceChangeEvent(operationUuid, ZERO, Metadata.of(metadata), operationDate, balance,
            null, false, CHANGE_STATUS);
    }

    public Map<String, Object> getBalanceInfo(Long id, String fields, String params) {
        BalanceDTO balanceDTO = findOne(id, null);
        if (balanceDTO == null) {
            throw new BusinessNotFoundException("error.balance.not.found", "Balance not found");
        }
        List<String> fieldList = splitToList(fields);
        Map<String, String> paramMap = splitToMap(params);

        Map<String, Object> info = new HashMap<>();
        info.put("id", balanceDTO.getId());
        for (String field : fieldList) {
            Object extension = self.extendBalanceInfo(balanceDTO, field, paramMap);
            ofNullable(extension).ifPresent(
                ext -> info.put(field, ext)
            );
        }

        return info;
    }

    @LogicExtensionPoint(value = "ExtendBalanceInfo", resolver = BalanceFieldKeyResolver.class)
    public Object extendBalanceInfo(BalanceDTO balanceDTO, String field, Map<String, String> params) {
        log.warn("No logic for field: {}", field);
        return null;
    }

    private List<String> splitToList(String fields) {
        String splitRegex = "\\s*,\\s*";
        fields = Objects.requireNonNullElse(fields, "");

        return Stream.of(fields.split(splitRegex))
            .collect(Collectors.toList());
    }

    private Map<String, String> splitToMap(String params) {
        String splitRegex = "\\s*,\\s*";
        params = Objects.requireNonNullElse(params, "");

        return Stream.of(params.split(splitRegex))
            .map(parameter -> parameter.split("=", 2))
            .collect(Collectors.toMap(p -> p[0], p -> p.length > 1 ? p[1] : ""));
    }
}
