package com.icthh.xm.ms.balance.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PocketChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private Long pocketId;
    private String pocketKey;
    private String pocketLabel;

    @Embedded
    private Metadata metadata = new Metadata();

    @Column(precision = 10, scale = 2)
    private BigDecimal amountDelta;

    @ManyToOne(optional = false)
    private BalanceChangeEvent transaction;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountBefore;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountAfter;

}
