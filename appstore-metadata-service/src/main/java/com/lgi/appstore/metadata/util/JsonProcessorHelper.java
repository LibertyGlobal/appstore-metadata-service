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
package com.lgi.appstore.metadata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgi.appstore.metadata.api.error.JsonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonProcessorHelper {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonProcessorHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T readValue(String target, String jsonString, Class<T> type) {
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            throw new JsonException(target, jsonString, e);
        }
    }

    public <T> T readValue(String target, String jsonString, TypeReference<T> type) {
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            throw new JsonException(target, jsonString, e);
        }
    }

    public String writeValueAsString(String target, Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException(target, null, e);
        }
    }
}
