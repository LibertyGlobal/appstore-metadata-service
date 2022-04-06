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
package com.lgi.appstore.metadata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgi.appstore.metadata.util.ApplicationUrlCreator;
import com.lgi.appstore.metadata.util.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Value("#{environment.BUNDLES_STORAGE_PROTOCOL}")
    private String bundlesStorageProtocol;

    @Value("#{environment.BUNDLES_STORAGE_HOST}")
    private String bundlesStorageHost;

    @Bean
    public ApplicationUrlCreator applicationUrlBuilder() {
        return new ApplicationUrlCreator(bundlesStorageProtocol, bundlesStorageHost);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.create();
    }
}
