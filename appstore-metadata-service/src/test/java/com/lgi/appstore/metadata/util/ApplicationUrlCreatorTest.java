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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationUrlCreatorTest {

    private static final String PROTOCOL = "http";
    private static final String BUNDLES_STORAGE_HOST = "bundles.host";
    private static final ApplicationUrlCreator APPLICATION_URL_CREATOR = new ApplicationUrlCreator(PROTOCOL, BUNDLES_STORAGE_HOST);

    @Test
    void canCreateApplicationUrl() {
        //GIVEN
        final String applicationId = UUID.randomUUID().toString();
        final String version = UUID.randomUUID().toString();
        final String platformName = UUID.randomUUID().toString();
        final String firmwareVer = UUID.randomUUID().toString();
        final ApplicationUrlCreator.NativeAppParams nativeAppParams = new ApplicationUrlCreator.NativeAppParams(applicationId, version, platformName, firmwareVer);

        //WHEN
        final String url = APPLICATION_URL_CREATOR.createApplicationUrl(nativeAppParams);

        //THEN
        assertThat(url).matches(PROTOCOL + "://" + BUNDLES_STORAGE_HOST.replace(".", "\\.") + "/"
                + applicationId + "/" + version + "/" + platformName + "/" + firmwareVer + "/"
                + applicationId + "_" + version + "_" + platformName + "_" + firmwareVer + "\\.tar\\.gz");
    }

    @Test
    void canCreateWebApplicationUrl() {
        // GIVEN
        final ApplicationUrlCreator.WebAppParams webAppParams = new ApplicationUrlCreator.WebAppParams("http://url");

        // WHEN
        final String url = APPLICATION_URL_CREATOR.createApplicationUrl(webAppParams);

        //THEN
        assertThat(url).isEqualTo("http://url");
    }

    @Test
    void cannotCreateWebApplicationIfUrlIsAbsent() {
        final var nullPointerException = assertThrows(NullPointerException.class,
                () -> new ApplicationUrlCreator.WebAppParams(null));
        assertThat(nullPointerException).hasMessage("sourceUrl");
    }

    @Test
    void cannotCreateNativeApplicationIfAnyParameterIsNull() {
        // WHEN
        final var missingFirmwareVersion = assertThrows(NullPointerException.class,
                () -> new ApplicationUrlCreator.NativeAppParams("appId", "version", "platform", null));
        final var missingPlatformName = assertThrows(NullPointerException.class,
                () -> new ApplicationUrlCreator.NativeAppParams("appId", "version", null, "firmware"));
        final var missingVersion = assertThrows(NullPointerException.class,
                () -> new ApplicationUrlCreator.NativeAppParams("appId", null, "platform", "firmware"));
        final var missingApplicationId = assertThrows(NullPointerException.class,
                () -> new ApplicationUrlCreator.NativeAppParams(null, "version", "platform", "firmware"));

        // THEN
        assertAll(() -> {
            assertThat(missingFirmwareVersion).hasMessage("firmwareVersion");
            assertThat(missingPlatformName).hasMessage("platformName");
            assertThat(missingVersion).hasMessage("version");
            assertThat(missingApplicationId).hasMessage("applicationId");
        });
    }

}