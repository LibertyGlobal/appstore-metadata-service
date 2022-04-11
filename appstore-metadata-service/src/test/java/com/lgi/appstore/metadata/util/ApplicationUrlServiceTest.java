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
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;

class ApplicationUrlServiceTest {
    private static final List<ApplicationType> WEB_APPLICATIONS = List.of(ApplicationType.HTML5);
    private final ApplicationUrlCreator urlCreator = Mockito.mock(ApplicationUrlCreator.class);
    private final ApplicationUrlService applicationUrlService = new ApplicationUrlService(urlCreator, WEB_APPLICATIONS);

    @Test
    void shouldGetUrlForWebApplication() {
        // GIVEN
        final var applicationUrlParams = createApplicationParams("application/vnd.rdk-app.html5");
        doReturn("http://web").when(urlCreator).createApplicationUrl(Mockito.any(ApplicationUrlCreator.WebAppParams.class));

        // WHEN
        final var actualUrl = applicationUrlService.createApplicationUrlFromApplicationRecord(applicationUrlParams);

        // THEN
        assertThat(actualUrl).isEqualTo("http://web");
    }

    @Test
    void shouldGetUrlForNativeApplication() {
        // GIVEN
        final var applicationUrlParams = createApplicationParams("any");
        doReturn("http://native").when(urlCreator).createApplicationUrl(Mockito.any(ApplicationUrlCreator.NativeAppParams.class));

        // WHEN
        final var actualUrl = applicationUrlService.createApplicationUrlFromApplicationRecord(applicationUrlParams);

        // THEN
        assertThat(actualUrl).isEqualTo("http://native");
    }

    private ApplicationUrlService.ApplicationUrlParams createApplicationParams(String appId) {
        return new ApplicationUrlService.ApplicationUrlParams(appId,
                null,
                null,
                null,
                null,
                null);
    }
}