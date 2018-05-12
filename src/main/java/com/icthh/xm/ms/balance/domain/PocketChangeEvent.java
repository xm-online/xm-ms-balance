package com.icthh.xm.ms.balance.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
public class PocketChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private Long pocketId;
    private String pocketKey;
    private String pocketLabel;

    @Column(precision=10, scale=2)
    private BigDecimal amountDelta;

    @ManyToOne(optional = false)
    private BalanceChangeEvent transaction;

}

