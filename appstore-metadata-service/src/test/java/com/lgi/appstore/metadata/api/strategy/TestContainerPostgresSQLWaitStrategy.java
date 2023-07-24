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
package com.lgi.appstore.metadata.api.strategy;

import com.lgi.appstore.metadata.api.delegate.TestContainerPostgresSQLDelegate;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess;

public class TestContainerPostgresSQLWaitStrategy extends AbstractWaitStrategy {
    private static final String TIMEOUT_ERROR = "Timed out waiting for PostgresSQL to be accessible for query execution";
    private final String user;
    private final String password;

    public TestContainerPostgresSQLWaitStrategy(final String user, final String password) {
        this.user = requireNonNull(user, "user");
        this.password = requireNonNull(password, "password");
    }

    @Override
    protected void waitUntilReady() {
        try {
            retryUntilSuccess((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
                getRateLimiter().doWhenReady(() -> {
                    try (TestContainerPostgresSQLDelegate databaseDelegate = getDatabaseDelegate()) {
                        databaseDelegate.selectOne();
                    }
                });
                return true;
            });
        } catch (Exception e) {
            throw new ContainerLaunchException(TIMEOUT_ERROR);
        }
    }

    private TestContainerPostgresSQLDelegate getDatabaseDelegate() {
        return new TestContainerPostgresSQLDelegate(waitStrategyTarget, user, password);
    }
}
