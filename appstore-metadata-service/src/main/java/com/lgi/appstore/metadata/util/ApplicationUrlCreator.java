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

package com.lgi.appstore.metadata.util;

import static java.util.Objects.requireNonNull;

public class ApplicationUrlCreator {

    //protocol://host/appId/appVersion/platformName/firmwareVer/appId_appVersion_platformName_firmwareVer.tar.gz
    private static final String APPLICATION_URL_PATTERN = "%s://%s/%s/%s/%s/%s/%s_%s_%s_%s.tar.gz";

    private final String protocol;
    private final String bundlesStorageHost;

    public ApplicationUrlCreator(String protocol, String bundlesStorageHost) {
        this.protocol = requireNonNull(protocol, "protocol");
        this.bundlesStorageHost = requireNonNull(bundlesStorageHost, "bundlesStorageHost");
    }

    public String createApplicationUrl(String applicationId, String version, String platformName, String firmwareVer) {
        return String.format(APPLICATION_URL_PATTERN,
                this.protocol,
                this.bundlesStorageHost,
                applicationId,
                version,
                platformName,
                firmwareVer,
                applicationId,
                version,
                platformName,
                firmwareVer);
    }
}
