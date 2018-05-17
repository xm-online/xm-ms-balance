package com.icthh.xm.ms.balance.domain;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Column(precision=10, scale=2)
    private BigDecimal amountDelta;

    @Enumerated(STRING)
    private OperationType operationType;
    private Instant operationDate;

    private String executedByUserKey;

    private String operationId;

    @OneToMany(mappedBy = "transaction", cascade = ALL)
    private List<PocketChangeEvent> pocketChangeEvents = new ArrayList<>();

    public void addPocketChangeEvent(PocketChangeEvent event) {
        event.setTransaction(this);
        pocketChangeEvents.add(event);
    }

}
