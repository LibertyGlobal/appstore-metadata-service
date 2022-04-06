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

import com.lgi.appstore.metadata.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringToCategoryConverterTest {

    private static final StringToCategoryConverter STRING_TO_CATEGORY_CONVERTER = new StringToCategoryConverter();

    @Test
    void canInstantiateCategoryApplication() {
        //GIVEN
        final String categoryAsString = "application";

        //WHEN
        final Category category = STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString);

        //THEN
        assertEquals(Category.APPLICATION, category);
    }

    @Test
    void canInstantiateCategoryDev() {
        //GIVEN
        final String categoryAsString = "dev";

        //WHEN
        final Category category = STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString);

        //THEN
        assertEquals(Category.DEV, category);
    }

    @Test
    void canInstantiateCategoryService() {
        //GIVEN
        final String categoryAsString = "service";

        //WHEN
        final Category category = STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString);

        //THEN
        assertEquals(Category.SERVICE, category);
    }

    @Test
    void canInstantiateCategoryResource() {
        //GIVEN
        final String categoryAsString = "resource";

        //WHEN
        final Category category = STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString);

        //THEN
        assertEquals(Category.RESOURCE, category);
    }

    @Test
    void canInstantiateCategoryPlugin() {
        //GIVEN
        final String categoryAsString = "plugin";

        //WHEN
        final Category category = STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString);

        //THEN
        assertEquals(Category.PLUGIN, category);
    }

    @Test
    void invalidCategoryResultsInAProperException() {
        //GIVEN
        final String categoryAsString = "fakeCategory";

        // WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString));

        // THEN
        String expectedMessage = "Unexpected value '" + categoryAsString + "'";
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void missingCategoryResultsInAProperException() {
        //GIVEN
        final String categoryAsString = null;

        // WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> STRING_TO_CATEGORY_CONVERTER.convert(categoryAsString));

        // THEN
        String expectedMessage = "Unexpected value 'null'";
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

}