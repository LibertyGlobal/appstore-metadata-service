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

package com.lgi.appstore.metadata.api.converter;

import com.lgi.appstore.metadata.model.Platform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringToPlatformConverterTest {

    private static final StringToPlatformConverter STRING_TO_PLATFORM_CONVERTER = new StringToPlatformConverter();

    @Test
    void canInstantiatePlatformWithArchitectureVariantAndOsPresent() {
        //GIVEN
        final String architecture = "arch";
        final String variant = "variant";
        final String os = "os";

        final String platformAsString = String.format("%s:%s:%s", architecture, variant, os);

        //WHEN
        final Platform platform = STRING_TO_PLATFORM_CONVERTER.convert(platformAsString);

        //THEN
        assertEquals(architecture, platform.getArchitecture());
        assertEquals(variant, platform.getVariant());
        assertEquals(os, platform.getOs());
    }

    @Test
    void canInstantiatePlatformWithMissingOs() {
        //GIVEN
        final String architecture = "arch";
        final String variant = "variant";
        final String os = "";

        final String platformAsString = String.format("%s:%s:%s", architecture, variant, os);

        //WHEN
        final Platform platform = STRING_TO_PLATFORM_CONVERTER.convert(platformAsString);

        //THEN
        assertEquals(architecture, platform.getArchitecture());
        assertEquals(variant, platform.getVariant());
        assertNull(platform.getOs());
    }

    @Test
    void canInstantiatePlatformWithMissingVariantAndOs() {
        //GIVEN
        final String architecture = "arch";
        final String variant = "";
        final String os = "";

        final String platformAsString = String.format("%s:%s:%s", architecture, variant, os);

        //WHEN
        final Platform platform = STRING_TO_PLATFORM_CONVERTER.convert(platformAsString);

        //THEN
        assertEquals(architecture, platform.getArchitecture());
        assertNull(platform.getVariant());
        assertNull(platform.getOs());
    }

    @Test
    void cannotInstantiatePlatformWithAllComponetsMissing() {
        //GIVEN
        final String architecture = "";
        final String variant = "";
        final String os = "";

        final String platformAsString = String.format("%s:%s:%s", architecture, variant, os);

        //WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> STRING_TO_PLATFORM_CONVERTER.convert(platformAsString));

        //THEN
        String expectedMessage = "Invalid platform value: " + platformAsString;
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void cannotInstantiatePlatformWithoutArchitecture() {
        //GIVEN
        final String architecture = "";
        final String variant = "variant";
        final String os = "";

        final String platformAsString = String.format("%s:%s:%s", architecture, variant, os);

        //WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> STRING_TO_PLATFORM_CONVERTER.convert(platformAsString));

        //THEN
        String expectedMessage = "Invalid platform value: " + platformAsString + " (architecture part is missing)";
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void cannotInstantiatePlatformWithTooManyComponents() {
        //GIVEN
        final String architecture = "architecture";
        final String variant = "variant";
        final String os = "os";

        final String platformAsString = String.format("%s:%s:%s:fake", architecture, variant, os);

        //WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> STRING_TO_PLATFORM_CONVERTER.convert(platformAsString));

        //THEN
        String expectedMessage = "Invalid platform value: " + platformAsString;
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

}