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

package com.lgi.appstore.metadata.api.testing.framework.base;

import com.lgi.appstore.metadata.api.testing.framework.infrastructure.TestDataStore;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.lgi.appstore.metadata.api.testing.framework.infrastructure.TestDataStore.DB_SCHEMA_NAME;

public class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext context) {
        TestDataStore dbTestContainer = TestDataStore.getInstance();
        ConfigurableEnvironment environment = context.getEnvironment();
        System.out.println(environment.getActiveProfiles());
        TestPropertyValues.of(
                "spring.datasource.url=" + dbTestContainer.withUrlParam("currentSchema", DB_SCHEMA_NAME).getJdbcUrl(),
                "spring.datasource.username=" + dbTestContainer.getUsername(),
                "spring.datasource.password=" + dbTestContainer.getPassword(),
                "spring.datasource.driver-class-name" + dbTestContainer.getDriverClassName(),
                "spring.datasource.hikari.maximum-pool-size=40",
                "spring.datasource.hikari.minimum-idle=5"
        ).applyTo(environment);
    }
}
