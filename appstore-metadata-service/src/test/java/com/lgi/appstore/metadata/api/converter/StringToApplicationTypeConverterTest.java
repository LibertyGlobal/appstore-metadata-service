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

package com.lgi.appstore.metadata.api.converter;

import com.lgi.appstore.metadata.model.ApplicationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringToApplicationTypeConverterTest {
    StringToApplicationTypeConverter converter = new StringToApplicationTypeConverter();

    @Test
    void canConvertStringToEnum() {
        //GIVEN
        final String type = "HTML5";

        //WHEN
        final var category = converter.convert(type);

        //THEN
        assertEquals(ApplicationType.HTML5, category);
    }

    @Test
    void missingCategoryResultsInAProperException() {
        //GIVEN
        final String type = null;
        final var expectedMessage = "Unexpected value 'null'";

        // WHEN
        final var expectedException = assertThrows(IllegalArgumentException.class, () -> converter.convert(type));

        // THEN
        var actualMessage = expectedException.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}