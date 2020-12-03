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

import com.fasterxml.jackson.core.JsonProcessingException;

import static com.lgi.appstore.metadata.api.test.framework.utils.DefaultObjectMapperFactory.configure;
import static com.lgi.appstore.metadata.api.test.framework.utils.DefaultObjectMapperFactory.newObjectMapper;

public class Serialization {
    public static <T> String toJson(T object) {
        try {
            return configure(newObjectMapper()).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJson(String json, Class<T> type) {
        try {
            return newObjectMapper().readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
