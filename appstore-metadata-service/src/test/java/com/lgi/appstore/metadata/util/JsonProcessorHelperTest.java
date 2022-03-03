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

import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.JsonObjectNames;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class JsonProcessorHelperTest {
    private static final String HARDWARE_JSON = "" +
            "{\n" +
            "      \"ram\": \"256\",\n" +
            "      \"dmips\": \"2000\",\n" +
            "      \"persistent\": \"20M\",\n" +
            "      \"cache\": \"50M\"\n" +
            "    }";

    private static final String HARDWARE_JSON_WITH_IMAGE_FIELD = "" +
            "{\n" +
            "      \"ram\": \"256\",\n" +
            "      \"dmips\": \"2000\",\n" +
            "      \"persistent\": \"20M\",\n" +
            "      \"cache\": \"50M\",\n" +
            "      \"image\": \"64M\"\n" +
            "    }";

    @ParameterizedTest
    @ValueSource(strings = {HARDWARE_JSON, HARDWARE_JSON_WITH_IMAGE_FIELD})
    void shouldDeserializeJsonRegardlessOfUnknownProperties(String json) {
        // GIVEN
        JsonProcessorHelper jsonProcessorHelper = new JsonProcessorHelper(ObjectMapperFactory.create());

        // WHEN
        final var hardware = jsonProcessorHelper.readValue(JsonObjectNames.HARDWARE, json, Hardware.class);

        // THEN
        assertThat(hardware)
                .hasFieldOrPropertyWithValue("ram", "256")
                .hasFieldOrPropertyWithValue("dmips", "2000")
                .hasFieldOrPropertyWithValue("persistent", "20M")
                .hasFieldOrPropertyWithValue("cache", "50M");
    }
}