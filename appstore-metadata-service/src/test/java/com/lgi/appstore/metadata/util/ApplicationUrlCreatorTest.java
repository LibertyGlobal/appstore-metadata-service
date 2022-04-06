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

        //WHEN
        final String url = APPLICATION_URL_CREATOR.createApplicationUrl(applicationId, version, platformName, firmwareVer);

        //THEN
        assertThat(url).matches(PROTOCOL + "://" + BUNDLES_STORAGE_HOST.replace(".", "\\.") + "/"
                + applicationId + "/" + version + "/" + platformName + "/" + firmwareVer + "/"
                + applicationId + "_" + version + "_" + platformName + "_" + firmwareVer + "\\.tar\\.gz");
    }
}