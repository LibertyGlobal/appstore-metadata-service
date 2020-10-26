/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 Liberty Global B.V.
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

import com.lgi.appstore.metadata.jooq.model.AppstoreMetadataService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.start();

        Configuration flywayConfiguration = new FluentConfiguration()
                .dataSource(POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        POSTGRE_SQL_CONTAINER.getUsername(),
                        POSTGRE_SQL_CONTAINER.getPassword())
                .schemas(AppstoreMetadataService.APPSTORE_METADATA_SERVICE.getName())
                .locations("classpath:db.migration");

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
