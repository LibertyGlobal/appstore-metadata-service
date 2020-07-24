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

package com.lgi.appstore.metadata.api.testing.functional.framework.steps;

import com.lgi.appstore.metadata.api.testing.functional.framework.infrastructure.TestDataStore;
import io.qameta.allure.Step;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.lgi.appstore.metadata.api.testing.functional.framework.infrastructure.TestDataStore.DB_SCHEMA_NAME;
import static com.lgi.appstore.metadata.api.testing.functional.framework.steps.MaintainerSteps.DEFAULT_MAINTAINER_CODE;

@Component
public class DbSteps {
    private static final Logger LOG = LoggerFactory.getLogger(DbSteps.class);
    private static final TestDataStore DB = TestDataStore.getInstance();

    private static final String ASMS_TABLE_NAME_APPLICATION = "application";
    private static final String ASMS_TABLE_NAME_MAINTAINER = "maintainer";

    @Step
    public void dbCleanup() throws SQLException {
        purgeTableApplication();
        purgeTableMaintainer();
    }

    @Step
    public void createNewMaintainer(String code) throws SQLException {
        LOG.info("Adding a new maintainer");
        String name = "Name_" + UUID.randomUUID();
        String address = "Address_" + UUID.randomUUID();
        String homepage = "Homepage_" + UUID.randomUUID();
        String email = "Email_" + UUID.randomUUID();
        Optional<RowSetDynaClass> rowSet = DB.performQuery(String.format("INSERT INTO %s.%s (code, name, address, homepage, email) " +
                "VALUES ('%s', '%s', '%s', '%s', '%s')", DB_SCHEMA_NAME, ASMS_TABLE_NAME_MAINTAINER, code, name, address, homepage, email));
        rowSet.ifPresent(this::logRows);
    }

    @Step
    public void listMaintainers() throws SQLException {
        listAllFromTable(ASMS_TABLE_NAME_MAINTAINER);
    }

    private void logRows(RowSetDynaClass rowSet) {
        List<DynaBean> rows = rowSet.getRows();
        rows.forEach(row -> Stream.of(rowSet.getDynaProperties())
                .forEach(column -> {
                    String columnName = column.getName();
                    LOG.info(String.format("Query result [%d]: %s = '%s'", rows.indexOf(row), columnName, row.get(columnName)));
                }));
    }

    private void purgeTableApplication() throws SQLException {
        purgeTable(ASMS_TABLE_NAME_APPLICATION);
    }

    private void purgeTableMaintainer() throws SQLException {
        LOG.info("Purging DB table: {}, leaving only hardcoded default entry for code = {}", ASMS_TABLE_NAME_MAINTAINER, DEFAULT_MAINTAINER_CODE);
        DB.performQuery(String.format("DELETE FROM %s.%s WHERE code <> '%s'", DB_SCHEMA_NAME, ASMS_TABLE_NAME_MAINTAINER, DEFAULT_MAINTAINER_CODE));
    }

    /**
     * @noinspection SameParameterValue as this might be reused for other tables
     */
    private void listAllFromTable(String tableName) throws SQLException {
        LOG.info("SELECT * FROM {}", tableName);

        Optional<RowSetDynaClass> rowSet = DB.performQuery(String.format("SELECT * FROM %s.%s", DB_SCHEMA_NAME, tableName));
        rowSet.ifPresent(this::logRows);
    }

    /**
     * @noinspection SameParameterValue as this might be reused for other tables
     */
    private void purgeTable(String tableName) throws SQLException {
        LOG.info("Purging DB table: {}", tableName);
        DB.performQuery(String.format("DELETE FROM %s.%s", DB_SCHEMA_NAME, tableName));
    }
}
