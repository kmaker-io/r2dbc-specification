package io.kmaker.r2dbcspecification.config;

import io.kmaker.r2dbcspecification.spec.R2dbcGenericSpecificationImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class R2dbcSpecificationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(R2dbcSpecificationAutoConfiguration.class));

    @Test
    void shouldContainsR2dbcSpecificationImplBean() {
        contextRunner
                .withUserConfiguration(TestConfig.class)
                .run(context -> {
                    assertTrue(context.containsBean("r2dbcGenericSpecification"));

                    final var r2dbcGenSpec = context.getBean("r2dbcGenericSpecification");
                    assertInstanceOf(R2dbcGenericSpecificationImpl.class, r2dbcGenSpec);
                });
    }

    @Configuration
    static class TestConfig {

        @Bean
        public R2dbcEntityTemplate r2dbcEntityTemplate() {
            return mock(R2dbcEntityTemplate.class);
        }
    }
}