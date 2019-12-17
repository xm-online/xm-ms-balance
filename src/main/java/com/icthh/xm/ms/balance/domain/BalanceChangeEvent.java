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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "balance_change_event")
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

    @NotNull
    @Column(name = "amount_delta", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountDelta;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @NotNull
    @Column(name = "operation_date", nullable = false)
    private Instant operationDate;

    @NotNull
    @Column(name = "executed_by_user_key", nullable = false)
    private String executedByUserKey;

    @NotNull
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @Embedded
    @Builder.Default
    private Metadata metadata = new Metadata();

    @Column(name = "amount_before", precision = 10, scale = 2)
    private BigDecimal amountBefore;

    @Column(name = "amount_after", precision = 10, scale = 2)
    private BigDecimal amountAfter;

    @NotNull
    @Column(name = "balance_id", nullable = false)
    private Long balanceId;

    @NotNull
    @Column(name = "balance_entity_id", nullable = false)
    private Long balanceEntityId;

    @NotNull
    @Column(name = "balance_key", nullable = false)
    private String balanceKey;

    @Column(name = "balance_type_key")
    private String balanceTypeKey;

    @Builder.Default
    @OneToMany(mappedBy = "transaction", cascade = ALL)
    private List<PocketChangeEvent> pocketChangeEvents = new ArrayList<>();

    public void addPocketChangeEvent(PocketChangeEvent event) {
        event.setTransaction(this);
        pocketChangeEvents.add(event);
    }
}
