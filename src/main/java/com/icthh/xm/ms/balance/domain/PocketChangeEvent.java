package com.icthh.xm.ms.balance.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pocket_change_event")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PocketChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "pocket_id", nullable = false)
    private Long pocketId;

    @NotNull
    @Column(name = "pocket_key", nullable = false)
    private String pocketKey;

    @NotNull
    @Column(name = "pocket_label", nullable = false)
    private String pocketLabel;

    @Embedded
    @Builder.Default
    private Metadata metadata = new Metadata();

    @Column(name = "amount_delta", precision = 10, scale = 2)
    private BigDecimal amountDelta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BalanceChangeEvent transaction;

    @Column(name = "amount_before", precision = 10, scale = 2)
    private BigDecimal amountBefore;

    @Column(name = "amount_after", precision = 10, scale = 2)
    private BigDecimal amountAfter;

}
