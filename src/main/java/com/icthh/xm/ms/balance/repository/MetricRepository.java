package com.icthh.xm.ms.balance.repository;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Metric;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Metric entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MetricRepository extends JpaRepository<Metric, Long>, ResourceRepository {

}
