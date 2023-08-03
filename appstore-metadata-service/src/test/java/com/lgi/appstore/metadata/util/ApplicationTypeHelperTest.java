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

import com.lgi.appstore.metadata.model.ApplicationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ApplicationTypeHelperTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "test")
    void shouldReturnFalseIfNotSupported(String type) {
        assertThat(ApplicationTypeHelper.isSupported(type)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationType.class)
    void shouldReturnTrueIfSupported(ApplicationType type) {
        // WHEN
        final var supported = ApplicationTypeHelper.isSupported(type.toString());

        // THEN
        assertThat(supported).isTrue();
    }

    @Test
    void canParseApplicationTypeInCaseInsensitiveWay() {
        // GIVEN
        final var webApps = List.of(ApplicationType.HTML5);

        // WHEN
        final var webApplication = ApplicationTypeHelper.isWebApplication("AppLiCation/vnd.rdk-app.HTML5", webApps);

        // THEN
        assertThat(webApplication).isTrue();
    }

    @Test
    void canReturnNativeApplication() {
        // GIVEN
        final var webApps = List.of(ApplicationType.HTML5);

        // WHEN
        final var nativeApplication = ApplicationTypeHelper.isNativeApplication("application/vnd.rdk-app.dac.native", webApps);

        // THEN
        assertThat(nativeApplication).isTrue();
    }

    @Test
    void canReturnWebApplication() {
        // GIVEN
        final var webApps = List.of(ApplicationType.HTML5);

        // WHEN
        final var webApplication = ApplicationTypeHelper.isWebApplication("application/vnd.rdk-app.html5", webApps);

        // THEN
        assertThat(webApplication).isTrue();
    }

    @Test
    void shouldReturnAndroidApplication() {
        // GIVEN
        // WHEN
        final var androidApplication = ApplicationTypeHelper.isAndroidApplication("application/apk");

        // THEN
        assertThat(androidApplication).isTrue();
    }

}