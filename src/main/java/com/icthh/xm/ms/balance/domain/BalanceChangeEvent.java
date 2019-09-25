package com.icthh.xm.ms.balance.domain;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

import com.icthh.xm.ms.balance.service.OperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "pocketChangeEvents")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private Long balanceId;
    private String balanceKey;
    private String balanceTypeKey;
    private Long balanceEntityId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountDelta;

    @Enumerated(STRING)
    private OperationType operationType;
    private Instant operationDate;

    private String executedByUserKey;

    private String operationId;

    @Embedded
    @Builder.Default
    private Metadata metadata = new Metadata();

    @Column(precision = 10, scale = 2)
    private BigDecimal amountBefore;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountAfter;

    @Builder.Default
    @OneToMany(mappedBy = "transaction", cascade = ALL)
    private List<PocketChangeEvent> pocketChangeEvents = new ArrayList<>();

    public void addPocketChangeEvent(PocketChangeEvent event) {
        event.setTransaction(this);
        pocketChangeEvents.add(event);
    }

}
