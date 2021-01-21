/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2021 Liberty Global Technology Services BV
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
package com.lgi.appstore.metadata.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppIdWithVersionTest {

    @Test
    void supportsAppIdWithExplicitVersion() {
        // GIVEN
        final String appId = "com.lgi.app";
        final String version = "1.2.3";
        final String appIdWithVersionString = String.format("%s:%s", appId, version);

        // WHEN
        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appIdWithVersionString);

        // THEN
        Assert.assertEquals(appId, appIdWithVersion.getAppId());
        Assert.assertEquals(version, appIdWithVersion.getVersion());
        Assert.assertFalse(appIdWithVersion.isLatest());
        Assert.assertFalse(appIdWithVersion.isAll());
    }

    @Test
    void supportsAppIdWithLatestVersion() {
        // GIVEN
        final String appId = "com.lgi.app";
        final String version = "latest";
        final String appIdWithVersionString = String.format("%s:%s", appId, version);

        // WHEN
        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appIdWithVersionString);

        // THEN
        Assert.assertEquals(appId, appIdWithVersion.getAppId());
        Assert.assertEquals(version, appIdWithVersion.getVersion());
        Assert.assertTrue(appIdWithVersion.isLatest());
        Assert.assertFalse(appIdWithVersion.isAll());
    }

    @Test
    void supportsAppIdWithoutVersion() {
        // GIVEN
        final String appId = "com.lgi.app";

        // WHEN
        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appId);

        // THEN
        Assert.assertEquals(appId, appIdWithVersion.getAppId());
        Assert.assertEquals(AppIdWithVersion.VERSION_LATEST, appIdWithVersion.getVersion());
        Assert.assertTrue(appIdWithVersion.isLatest());
        Assert.assertFalse(appIdWithVersion.isAll());
    }

    @Test
    void supportsAppIdWithAllVersion() {
        // GIVEN
        final String appId = "com.lgi.app";
        final String version = "all";
        final String appIdWithVersionString = String.format("%s:%s", appId, version);

        // WHEN
        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appIdWithVersionString);

        // THEN
        Assert.assertEquals(appId, appIdWithVersion.getAppId());
        Assert.assertEquals(AppIdWithVersion.VERSION_ALL, appIdWithVersion.getVersion());
        Assert.assertFalse(appIdWithVersion.isLatest());
        Assert.assertTrue(appIdWithVersion.isAll());
    }

    @Test()
    void willThrowAnExceptionIfMoreThanTwoTokensArePresent() {
        // GIVEN
        final String appId = "com.lgi.app:1.2.3:fake";

        // WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> AppIdWithVersion.fromString(appId));

        // THEN
        String expectedMessage = "Invalid app id with version format";
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test()
    void willThrowAnExceptionIfLessThanTwoTokensArePresent() {
        // GIVEN
        final String appId = "com.lgi.app:";

        // WHEN
        Exception expectedException = assertThrows(IllegalArgumentException.class, () -> AppIdWithVersion.fromString(appId));

        // THEN
        String expectedMessage = "No version provided";
        String actualMessage = expectedException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}