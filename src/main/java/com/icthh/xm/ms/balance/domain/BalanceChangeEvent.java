package com.icthh.xm.ms.balance.domain;

import static javax.persistence.CascadeType.ALL;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
public class BalanceChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private Long balanceId;
    private String balanceKey;
    private String balanceTypeKey;

    @Column(precision=10, scale=2)
    private BigDecimal amountDelta;

    private OperationType operationType;
    private Instant operationDate;

    private String executedByUserKey;

    @OneToMany(mappedBy = "transaction", cascade = ALL)
    private List<PocketChangeEvent> pocketHistory = new ArrayList<>();

}
