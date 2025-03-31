package io.kmaker.r2dbcspecification;

import io.r2dbc.spi.ConnectionFactories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.H2Dialect;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@TestConfiguration
@Slf4j
public class R2dbcTestConfig {

    @Bean
    public static R2dbcEntityTemplate r2dbcEntityTemplate() {
        final var connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        final var databaseClient = DatabaseClient.create(connectionFactory);
        return new R2dbcEntityTemplate(databaseClient, H2Dialect.INSTANCE);
    }

    @Bean
    public static TransactionalOperator transactionalOperator(final R2dbcEntityTemplate r2dbcEntityTemplate) {
        final var transactionManager = new R2dbcTransactionManager(r2dbcEntityTemplate.getDatabaseClient().getConnectionFactory());
        return TransactionalOperator.create(transactionManager);
    }

    public static void initData(final R2dbcEntityTemplate template) {
        try {
            final var sampleData = R2dbcTestConfig.class.getResourceAsStream("/sample_data.sql");
            Assert.notNull(sampleData, "sampleData is null");
            final var buffered = new BufferedReader(new InputStreamReader(sampleData));

            final var sqlBuilder = new StringBuilder();
            String line;

            while ((line = buffered.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            final var sqlCommands = sqlBuilder.toString().split(";");
            for (final var sqlCommand : sqlCommands) {
                if (!sqlCommand.trim().isBlank()) {
                    template.getDatabaseClient()
                            .sql(sqlCommand)
                            .fetch()
                            .rowsUpdated()
                            .block();
                }
            }
            log.info("Init sample data successful...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
