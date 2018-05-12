package com.icthh.xm.ms.balance.repository;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BalanceChangeEventRepository extends JpaRepository<BalanceChangeEvent, Long>, JpaSpecificationExecutor<BalanceChangeEvent>, ResourceRepository {

}
