package com.icthh.xm.ms.balance.repository;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Pocket;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Pocket entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PocketRepository extends JpaRepository<Pocket, Long>, ResourceRepository {

}
