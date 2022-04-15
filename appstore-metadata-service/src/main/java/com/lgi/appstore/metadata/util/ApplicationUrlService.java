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

import java.util.Collection;
import java.util.Objects;

public class ApplicationUrlService {
    private final ApplicationUrlCreator urlCreator;
    private final Collection<ApplicationType> webApplications;

    public ApplicationUrlService(ApplicationUrlCreator urlCreator, Collection<ApplicationType> webApplications) {
        this.urlCreator = urlCreator;
        this.webApplications = webApplications;
    }

    public String createApplicationUrlFromApplicationRecord(ApplicationUrlParams applicationUrlParams) {
        return ApplicationTypeHelper.isWebApplication(applicationUrlParams.type, webApplications) ?
                urlCreator.createApplicationUrl(applicationUrlParams.webApp()) :
                urlCreator.createApplicationUrl(applicationUrlParams.nativeApp());
    }

    public static class ApplicationUrlParams {
        private final String platformName;
        private final String firmwareVersion;
        private final String applicationId;
        private final String version;
        private final String sourceUrl;
        private final String type;

        public ApplicationUrlParams(String type, String platformName, String firmwareVersion, String applicationId, String version, String imageUrl) {
            this.type = type;
            this.platformName = platformName;
            this.firmwareVersion = firmwareVersion;
            this.applicationId = applicationId;
            this.version = version;
            this.sourceUrl = imageUrl;
        }

        public ApplicationUrlCreator.NativeAppParams nativeApp() {
            return new ApplicationUrlCreator.NativeAppParams(applicationId, version, platformName, firmwareVersion);
        }

        public ApplicationUrlCreator.WebAppParams webApp() {
            return new ApplicationUrlCreator.WebAppParams(this.sourceUrl);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApplicationUrlParams that = (ApplicationUrlParams) o;
            return Objects.equals(platformName, that.platformName) &&
                    Objects.equals(firmwareVersion, that.firmwareVersion) &&
                    Objects.equals(applicationId, that.applicationId) &&
                    Objects.equals(version, that.version) &&
                    Objects.equals(sourceUrl, that.sourceUrl) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(platformName, firmwareVersion, applicationId, version, sourceUrl, type);
        }
    }
}
