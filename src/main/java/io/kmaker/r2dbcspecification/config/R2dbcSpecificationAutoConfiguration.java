package io.kmaker.r2dbcspecification.config;

import io.kmaker.r2dbcspecification.spec.R2dbcGenericSpecificationImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@AutoConfiguration(after = R2dbcRepositoriesAutoConfiguration.class)
@ConditionalOnBean(R2dbcEntityTemplate.class)
public class R2dbcSpecificationAutoConfiguration {

    @Bean(name = "r2dbcGenericSpecification")
    @ConditionalOnMissingBean
    public R2dbcGenericSpecificationImpl r2dbcGenericSpecification(final R2dbcEntityTemplate r2dbcEntityTemplate) {
        return new R2dbcGenericSpecificationImpl(r2dbcEntityTemplate);
    }

}
