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

package com.lgi.appstore.metadata.api.test.framework.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.restassured.mapper.factory.Jackson2ObjectMapperFactory;

import java.lang.reflect.Type;

public class DefaultObjectMapperFactory implements Jackson2ObjectMapperFactory {
    /**
     * Add configuration settings to {@link ObjectMapper} to:
     * - exclude "null" values from JSON,
     * - exclude absent/empty values,
     * - not write timestamps for dates.
     *
     * @param objectMapper to configure
     * @return the original ObjectMapper
     */
    static ObjectMapper configure(ObjectMapper objectMapper) {
        return objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new Jdk8Module());
    }

    static ObjectMapper newObjectMapper() {
        return configure(new ObjectMapper());
    }

    @Override
    public ObjectMapper create(Type type, String s) {
        return newObjectMapper();
    }
}
