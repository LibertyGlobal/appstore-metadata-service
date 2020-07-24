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

package com.lgi.appstore.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AppIdWithVersion {

    private static final String APP_ID_VERSION_SEPARATOR = ":";
    static final String VERSION_LATEST = "latest";
    static final String VERSION_ALL = "all";

    private final String appId;
    private final String version;
    private final boolean latest;
    private final Boolean all;

    private AppIdWithVersion(String appId, String version, Boolean latest, Boolean all) {
        this.appId = appId;
        this.version = version;
        this.latest = latest;
        this.all = all;
    }

    @JsonCreator
    public static AppIdWithVersion fromString(String appIdWithVersion) {
        final String appId;
        final String version;
        final boolean latest;
        final boolean all;
        if (appIdWithVersion.contains(APP_ID_VERSION_SEPARATOR)) {
            final String[] tokens = appIdWithVersion.split(APP_ID_VERSION_SEPARATOR);
            appId = tokens[0];

            if (tokens.length > 2) {
                throw new IllegalArgumentException("Invalid app id with version format");
            }

            if (tokens.length == 1 || tokens[1].isBlank()) {
                throw new IllegalArgumentException("No version provided");
            }

            if (tokens[1].equalsIgnoreCase(VERSION_LATEST)) {
                latest = true;
                all = false;
                version = null;
            } else if (tokens[1].equalsIgnoreCase(VERSION_ALL)) {
                all = true;
                latest = false;
                version = null;
            } else {
                all = false;
                latest = false;
                version = tokens[1];
            }
        } else {
            appId = appIdWithVersion;
            version = null;
            latest = true;
            all = false;
        }
        return new AppIdWithVersion(appId, version, latest, all);
    }

    public String getAppId() {
        return appId;
    }

    public String getVersion() {
        if (isLatest()) {
            return VERSION_LATEST;
        } else if (isAll()) {
            return VERSION_ALL;
        } else {
            return version;
        }
    }

    public boolean isLatest() {
        return latest;
    }

    public boolean isAll() {
        return all;
    }
}
