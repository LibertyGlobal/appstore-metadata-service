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

package com.lgi.appstore.metadata.api.test.framework.base;

import com.lgi.appstore.metadata.api.test.framework.infrastructure.TestDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;

public class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private Logger LOG = LoggerFactory.getLogger(DataSourceInitializer.class);

    public void initialize(ConfigurableApplicationContext context) {
        final ConfigurableEnvironment environment = context.getEnvironment();
        LOG.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));

        TestDataStore dbTestContainer = TestDataStore.getInstance(environment);
        String dbUrl = dbTestContainer.withUrlParam("currentSchema", dbTestContainer.getDatabaseSchemaName()).getJdbcUrl();
        String dbUsername = dbTestContainer.getUsername();
        String dbPassword = dbTestContainer.getPassword();
        LOG.info("dbUrl: {}", dbUrl);
        LOG.info("dbUsername: {}", dbUsername);
        LOG.info("dbPassword: {}", dbPassword);

        TestPropertyValues.of(
                "spring.datasource.url=" + dbUrl,
                "spring.datasource.username=" + dbUsername,
                "spring.datasource.password=" + dbPassword,
                "spring.datasource.driver-class-name" + dbTestContainer.getDriverClassName(),
                "spring.datasource.hikari.maximum-pool-size=40",
                "spring.datasource.hikari.minimum-idle=5",
                "spring.groovy.template.check-template-location=false"
        ).applyTo(environment);
    }
}
