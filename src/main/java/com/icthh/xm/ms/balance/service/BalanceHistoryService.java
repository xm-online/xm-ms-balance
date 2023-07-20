package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.repository.CriteriaPermittedRepository;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.PocketChangeEventRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.BalanceHistoryCriteria;
import com.icthh.xm.ms.balance.service.mapper.BalanceChangeEventMapper;
import com.icthh.xm.ms.balance.web.rest.requests.HistoryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@LepService(group = "service")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BalanceHistoryService {

    private final BalanceChangeEventRepository balanceChangeEventRepository;
    private final PocketChangeEventRepository pocketChangeEventRepository;
    private final CriteriaPermittedRepository permittedRepository;
    private final BalanceChangeEventMapper mapper;
    private BalanceHistoryService self;

    @LogicExtensionPoint("GetBalanceChangesByTypeAndDate")
    public Page<BalanceChangeEvent> getBalanceChangesByTypeAndDate(HistoryRequest historyRequest, Pageable pageable) {
        return balanceChangeEventRepository.findByEntityIdInAndOperationTypeAndOperationDateBetween(
            historyRequest.getEntityIds(), historyRequest.getOperationType(), historyRequest.getStartDate(),
            historyRequest.getEndDate(), pageable);
    }

    public Page<BalanceChangeEvent> findBalanceChanges(String templateName,
                                                       Map<String, Object> params,
                                                       Pageable pageable) {
        return balanceChangeEventRepository.findAll(self.balanceChangesSearchTemplate(templateName, params), pageable);
    }


    public Page<BalanceChangeEventDto> getBalanceChangesByCriteria(BalanceHistoryCriteria criteria, Pageable pageable, String privilegeKey) {
        if (criteria.getOperationType() != null) {
            FilterUtils.remapEnumFilter(criteria.getOperationType(), OperationType.class);
        }
        Page<BalanceChangeEvent> page = permittedRepository.findWithPermission(BalanceChangeEvent.class, criteria, pageable, privilegeKey);
        return new PageImpl<>(mapper.toDto(page.getContent()), pageable, page.getTotalElements());
    }

    @LogicExtensionPoint(value = "BalanceChangesSearchTemplate", resolver = TemplateResolver.class)
    public Specification<BalanceChangeEvent> balanceChangesSearchTemplate(String templateName,
                                                                          Map<String, Object> params) {
        throw new EntityNotFoundException("Balance changes search template " + templateName + " not found");
    }

    @LogicExtensionPoint("GetPocketChangesByTypeAndDate")
    public Page<PocketChangeEvent> getPocketChangesByTypeAndDate(HistoryRequest historyRequest, Pageable pageable) {
        return pocketChangeEventRepository.findByEntityIdInAndOperationTypeAndOperationDateBetween(
            historyRequest.getEntityIds(), historyRequest.getOperationType(), historyRequest.getStartDate(),
            historyRequest.getEndDate(), pageable);
    }

    public Page<PocketChangeEvent> findPocketChanges(String templateName,
                                                     Map<String, Object> params,
                                                     Pageable pageable) {
        return pocketChangeEventRepository.findAll(self.pocketChangesSearchTemplate(templateName, params), pageable);
    }

    @LogicExtensionPoint(value = "PocketChangesSearchTemplate", resolver = TemplateResolver.class)
    public Specification<PocketChangeEvent> pocketChangesSearchTemplate(String templateName,
                                                                        Map<String, Object> params) {
        throw new EntityNotFoundException("Pocket changes search template " + templateName + " not found");
    }

    @Autowired
    public void setOperationHistoryService(BalanceHistoryService self) {
        this.self = self;
    }

}
