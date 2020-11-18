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

package com.lgi.appstore.metadata.config;

import com.lgi.appstore.metadata.info.AppProperty;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.lgi.appstore.metadata.api.constants.AppConstants.MISSING_DETAIL_VALUE;

@Component
public class MeterRegistryConfig {
    private final Environment environment;

    public MeterRegistryConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry
                .config()
                .commonTags("application", getAppNameFromEnvironment());
    }

    private String getAppNameFromEnvironment() {
        return Optional.ofNullable(environment.getProperty(AppProperty.APP_NAME.toString()))
                .orElse(MISSING_DETAIL_VALUE);
    }
}
