package com.icthh.xm.ms.balance.repository;

import com.github.database.rider.spring.api.DBRider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@DBRider
@ActiveProfiles("dao-test")
public abstract class BaseDaoTest {
    @Autowired
    protected TestEntityManager manager;

    @AfterEach
    public void flush() {
        manager.flush();
    }
}
