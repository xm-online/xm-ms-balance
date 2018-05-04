package com.icthh.xm.ms.balance.repository;

import com.github.database.rider.spring.api.DBRider;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase
@DBRider
@ActiveProfiles("dao-test")
public abstract class BaseDaoTest {
    @Autowired
    protected TestEntityManager manager;

    @After
    public void flush() {
        manager.flush();
    }
}
