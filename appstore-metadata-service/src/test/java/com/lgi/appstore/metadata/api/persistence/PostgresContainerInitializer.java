/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2021 Liberty Global Technology Services BV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lgi.appstore.metadata.api.persistence;

import com.lgi.appstore.metadata.jooq.model.DefaultCatalog;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jooq.Schema;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:12");
        POSTGRE_SQL_CONTAINER.start();

        final List<Schema> schemas = DefaultCatalog.DEFAULT_CATALOG.getSchemas();
        if (schemas.size() != 1) {
            throw new IllegalStateException("There should be exactly one schema");
        }

        Configuration flywayConfiguration = new FluentConfiguration()
                .dataSource(POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        POSTGRE_SQL_CONTAINER.getUsername(),
                        POSTGRE_SQL_CONTAINER.getPassword())
                .schemas(schemas.get(0).getName())
                .locations("classpath:db/migration");

        Flyway flyway = new Flyway(flywayConfiguration);
        flyway.migrate();
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.url=" + POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRE_SQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + POSTGRE_SQL_CONTAINER.getPassword());
    }
}
