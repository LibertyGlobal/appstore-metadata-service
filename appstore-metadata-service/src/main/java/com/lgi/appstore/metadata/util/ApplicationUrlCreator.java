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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ApplicationUrlCreator {
    //protocol://host/appId/appVersion/platformName/firmwareVer/appId-appVersion-platformName-firmwareVer.tar.gz
    private static final String APPLICATION_URL_PATTERN = "%s://%s/%s/%s/%s/%s/%s-%s-%s-%s.tar.gz";

    private final String protocol;
    private final String bundlesStorageHost;

    public ApplicationUrlCreator(String protocol, String bundlesStorageHost) {
        this.protocol = requireNonNull(protocol, "protocol");
        this.bundlesStorageHost = requireNonNull(bundlesStorageHost, "bundlesStorageHost");
    }

    public String createApplicationUrl(NativeAppParams nativeAppParams) {
        return createNativeApplicationUrl(nativeAppParams.getApplicationId(),
                nativeAppParams.getVersion(),
                nativeAppParams.getPlatformName(),
                nativeAppParams.getFirmwareVersion());
    }

    public String createApplicationUrl(WebAppParams webAppParams) {
        return webAppParams.getSourceUrl();
    }

    private String createNativeApplicationUrl(String applicationId, String version, String platformName, String firmwareVer) {
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

    public static class NativeAppParams {
        private final String applicationId;
        private final String version;
        private final String platformName;
        private final String firmwareVersion;

        public NativeAppParams(String applicationId, String version, String platformName, String firmwareVersion) {
            this.applicationId = requireNonNull(applicationId, "applicationId");
            this.version = requireNonNull(version, "version");
            this.platformName = requireNonNull(platformName, "platformName");
            this.firmwareVersion = requireNonNull(firmwareVersion, "firmwareVersion");
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getVersion() {
            return version;
        }

        public String getPlatformName() {
            return platformName;
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NativeAppParams that = (NativeAppParams) o;
            return Objects.equals(applicationId, that.applicationId) && Objects.equals(version, that.version) && Objects.equals(platformName, that.platformName) && Objects.equals(firmwareVersion, that.firmwareVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(applicationId, version, platformName, firmwareVersion);
        }
    }

    public static class WebAppParams {
        private final String sourceUrl;

        public WebAppParams(String sourceUrl) {
            this.sourceUrl = requireNonNull(sourceUrl, "sourceUrl");
        }

        public String getSourceUrl() {
            return sourceUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WebAppParams that = (WebAppParams) o;
            return Objects.equals(sourceUrl, that.sourceUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceUrl);
        }
    }
}
