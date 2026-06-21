package com.mttauto.momentum_api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TestDatabaseIsolationTest {

    @Autowired
    private Environment environment;

    @Test
    void testsUseInMemoryDatabase() {
        assertThat(environment.getProperty("spring.datasource.url"))
                .startsWith("jdbc:h2:mem:");
    }
}
