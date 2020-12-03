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

package com.lgi.appstore.metadata.api.test.framework.infrastructure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class TestDataStore extends PostgreSQLContainer<TestDataStore> {
    private static final Logger LOG = LoggerFactory.getLogger(TestDataStore.class);

    private static final String DB_SCHEMA_NAME_DEFAULT = "appstore_metadata_service";

    private static final String DB_IMAGE_NAME = "postgres:11.1";
    private static final String DB_NAME = "postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    private static TestDataStore singleton;

    private DataSource ds;
    private String databaseSchemaName;

    private TestDataStore(PropertyResolver propertyResolver) {
        super(DB_IMAGE_NAME);

        databaseSchemaName = propertyResolver.getProperty("db.schema", String.class, DB_SCHEMA_NAME_DEFAULT);
    }

    public static TestDataStore getInstance(PropertyResolver propertyResolver) {
        if (singleton == null) {
            singleton = new TestDataStore(propertyResolver)
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD);

            singleton.superStart(); // spawn just a single instance for all tests
        }

        return singleton;
    }

    private void superStart() {
        super.start();
        ds = initDataSource();
    }

    @Override
    public void start() {
        //started during init so do nothing here
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down when tests finish
    }

    public Optional<RowSetDynaClass> performQuery(String sqlCmd) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                boolean resultSetReceived = statement.execute(sqlCmd);
                if (resultSetReceived) {
                    try (ResultSet resultSet = statement.getResultSet()) {
                        LOG.info("SQL execution returned a set of results.");
                        return Optional.of(new RowSetDynaClass(resultSet));
                    }
                } else {
                    LOG.info("SQL execution resulted in updates only, count={}", statement.getUpdateCount());
                    return Optional.empty();
                }
            }
        }
    }

    public String getDatabaseSchemaName() {
        return databaseSchemaName;
    }

    private static DataSource initDataSource() {
        String jdbcUrl = singleton.getJdbcUrl();
        LOG.info("JDBC url={}", jdbcUrl);
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setJdbcUrl(jdbcUrl);
        poolConfig.setUsername(singleton.getUsername());
        poolConfig.setPassword(singleton.getPassword());
        poolConfig.setDriverClassName(singleton.getDriverClassName());
        poolConfig.setMaximumPoolSize(40);
        HikariDataSource hikariDataSource = new HikariDataSource(poolConfig);
        LOG.info("Datasource for tests created.");
        return hikariDataSource;
    }
}