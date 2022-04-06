/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 Liberty Global Technology Services BV
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
package com.lgi.appstore.metadata.test.framework.base;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

public class EnvPropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String BUNDLES_STORAGE_PROTOCOL_ENV_PROPERTY_NAME = "BUNDLES_STORAGE_PROTOCOL";
    private static final String BUNDLES_STORAGE_HOST_ENV_PROPERTY_NAME = "BUNDLES_STORAGE_HOST";
    private static final String DEFAULT_BUNDLES_STORAGE_PROTOCOL = "http";
    private static final String DEFAULT_BUNDLES_STORAGE_HOST = "localhost";

    public void initialize(ConfigurableApplicationContext context) {
        final ConfigurableEnvironment environment = context.getEnvironment();

        final List<String> properties = new ArrayList<>();

        if (environment.getProperty(BUNDLES_STORAGE_PROTOCOL_ENV_PROPERTY_NAME) == null) {
            properties.add(BUNDLES_STORAGE_PROTOCOL_ENV_PROPERTY_NAME + "=" + DEFAULT_BUNDLES_STORAGE_PROTOCOL);
        }

        if (environment.getProperty(BUNDLES_STORAGE_HOST_ENV_PROPERTY_NAME) == null) {
            properties.add(BUNDLES_STORAGE_HOST_ENV_PROPERTY_NAME + "=" + DEFAULT_BUNDLES_STORAGE_HOST);
        }

        TestPropertyValues.of(properties).applyTo(environment);
    }
}
