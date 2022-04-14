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

package com.lgi.appstore.metadata.api.stb.input.validator;

import com.lgi.appstore.metadata.api.error.MandatoryFieldForNativeAppNotFound;
import com.lgi.appstore.metadata.api.error.UnsupportedApplicationTypeException;
import com.lgi.appstore.metadata.api.stb.input.StbAppsListParams;
import com.lgi.appstore.metadata.model.AppIdWithType;
import com.lgi.appstore.metadata.model.ApplicationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformAndVersionOptionalForWebValidatorTest {
    private static final List<ApplicationType> WEB_APPLICATIONS = List.of(ApplicationType.HTML5);

    private final PlatformAndVersionOptionalForWebValidator validator = new PlatformAndVersionOptionalForWebValidator(WEB_APPLICATIONS);

    @Test
    void throwExceptionIfAppIdNotSet() {
        final var emptyStbAppParams = new StbAppsListParams();
        assertThrows(NullPointerException.class, () -> validator.validate(emptyStbAppParams, null));
    }

    @Test
    void throwExceptionIfNativeAppDoesNotHavePlatformName() {
        // GIVEN
        final var nativeAppStbParams = new StbAppsListParams("native-app");
        final var appIdWithType = new AppIdWithType("native-app", ApplicationType.DAC_NATIVE.getValue());

        // WHEN
        final var exception = assertThrows(MandatoryFieldForNativeAppNotFound.class,
                () -> validator.validate(nativeAppStbParams, appIdWithType));

        // THEN
        assertThat(exception).hasMessage("platformName is mandatory for native apps");
    }

    @Test
    void throwExceptionIfNativeAppDoesNotHaveFirmwareVersion() {
        // GIVEN
        final var nativeAppParams = new StbAppsListParams("native-app");
        nativeAppParams.setPlatformName("platform");
        final var appIdWithType = new AppIdWithType("native-app", ApplicationType.DAC_NATIVE.getValue());

        // WHEN
        final var exception = assertThrows(MandatoryFieldForNativeAppNotFound.class,
                () -> validator.validate(nativeAppParams, appIdWithType));

        // THEN
        assertThat(exception).hasMessage("firmwareVer is mandatory for native apps");
    }

    @Test
    void shouldReturnNativeAppIfAllFieldsAreSet() {
        // GIVEN
        final var nativeAppParams = new StbAppsListParams("native-app");
        nativeAppParams.setPlatformName("platform");
        nativeAppParams.setFirmwareVer("firmware");

        // WHEN
        final var result = validator.validate(nativeAppParams, new AppIdWithType("native-app", ApplicationType.DAC_NATIVE.getValue()));

        // THEN
        assertThat(result.platformAndVersionMustBeIgnored()).isFalse();
    }

    @Test
    void shouldReturnWebAppWithoutPlatformAndFirmware() {
        // GIVEN
        final var nativeApp = new StbAppsListParams("web-app");
        nativeApp.setPlatformName("platform");
        nativeApp.setFirmwareVer("firmware");

        // WHEN
        final var result = validator.validate(nativeApp,  new AppIdWithType("web-app", ApplicationType.HTML5.getValue()));

        // THEN
        assertThat(result.platformAndVersionMustBeIgnored()).isTrue();
    }

    @Test
    void throwExceptionIfApplicationIsNotSupported() {
        // GIVEN
        final var nativeAppParams = new StbAppsListParams("native-app");
        nativeAppParams.setPlatformName("platform");
        final var appIdWithType = new AppIdWithType("native-app", null);

        // WHEN
        final var exception = assertThrows(UnsupportedApplicationTypeException.class,
                () -> validator.validate(nativeAppParams, appIdWithType));

        // THEN
        assertThat(exception).hasMessage("Unsupported application type");
    }

}