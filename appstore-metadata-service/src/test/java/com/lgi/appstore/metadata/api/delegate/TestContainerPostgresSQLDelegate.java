/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2023 Liberty Global Technology Services BV
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
package com.lgi.appstore.metadata.api.delegate;

import org.testcontainers.containers.ContainerState;
import org.testcontainers.delegate.AbstractDatabaseDelegate;
import org.testcontainers.exception.ConnectionCreationException;
import org.testcontainers.ext.ScriptUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class TestContainerPostgresSQLDelegate extends AbstractDatabaseDelegate<Connection> {
    private final static String USER_PROPERTY_KEY = "user";
    private final static String PASSWORD_PROPERTY_KEY = "password";
    private final static String JDBC_URL_TEMPLATE = "jdbc:postgresql://localhost:%s/test";
    private static final String SELECT_ONE_QUERY = "SELECT 1";
    private final static String CONNECTION_CREATION_EXCEPTION_MSG = "Could not obtain PostgresSQL connection";
    private final ContainerState container;
    private final String user;
    private final String password;
    public TestContainerPostgresSQLDelegate(final ContainerState container, final String user, final String password) {
        this.container = requireNonNull(container, "container");
        this.user = requireNonNull(user, "user");
        this.password = requireNonNull(password, "password");
    }

    @Override
    protected Connection createNewConnection() {
        try {
            Properties connectionProps = new Properties();
            connectionProps.put(USER_PROPERTY_KEY, user);
            connectionProps.put(PASSWORD_PROPERTY_KEY, password);
            return DriverManager.getConnection(
                    String.format(JDBC_URL_TEMPLATE, container.getFirstMappedPort()),
                    connectionProps);
        } catch (Exception e) {
            throw new ConnectionCreationException(CONNECTION_CREATION_EXCEPTION_MSG, e);
        }
    }

    @Override
    public void execute(String statement, String scriptPath, int lineNumber, boolean continueOnError, boolean ignoreFailedDrops) {
        try {
            getConnection().prepareStatement(statement).executeQuery();
        } catch (Exception e) {
            throw new ScriptUtils.ScriptStatementFailedException(statement, lineNumber, scriptPath, e);
        }
    }

    public void selectOne() {
        execute(SELECT_ONE_QUERY, "", 1, false, false);
    }

    @Override
    protected void closeConnectionQuietly(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
