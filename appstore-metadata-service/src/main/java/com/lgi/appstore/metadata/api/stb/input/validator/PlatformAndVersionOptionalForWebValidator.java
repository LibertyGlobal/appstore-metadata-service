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
import com.lgi.appstore.metadata.util.ApplicationTypeHelper;

import java.util.Collection;
import java.util.Objects;

import static com.lgi.appstore.metadata.util.ApplicationTypeHelper.isNativeApplication;
import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.util.Strings.isBlank;

public class PlatformAndVersionOptionalForWebValidator {
    private final Collection<ApplicationType> webApplications;

    public PlatformAndVersionOptionalForWebValidator(Collection<ApplicationType> webApplications) {
        this.webApplications = requireNonNull(webApplications, "webApplications");
    }

    public PlatformAndVersionValidationResult validate(StbAppsListParams stbAppsListParams, AppIdWithType appIdWithType) {
        requireNonNull(stbAppsListParams.getAppId(), "stbAppsListParams");
        requireNonNull(appIdWithType, "appIdWithType");

        final var applicationType = appIdWithType.getApplicationType();

        if (!ApplicationTypeHelper.isSupported(applicationType)) {
            throw new UnsupportedApplicationTypeException();
        }
        if (isNativeApplication(applicationType, webApplications)) {
            if (isBlank(stbAppsListParams.getPlatformName())) {
                throw new MandatoryFieldForNativeAppNotFound("platformName");
            }
            if (isBlank(stbAppsListParams.getFirmwareVer())) {
                throw new MandatoryFieldForNativeAppNotFound("firmwareVer");
            }
            return new PlatformAndVersionValidationResult(false);
        }
        return new PlatformAndVersionValidationResult(true);
    }

    public static class PlatformAndVersionValidationResult {
        private final boolean platformAndVersionMustBeIgnored;

        public PlatformAndVersionValidationResult(boolean platformAndVersionMustBeIgnored) {
            this.platformAndVersionMustBeIgnored = platformAndVersionMustBeIgnored;
        }

        public boolean platformAndVersionMustBeIgnored() {
            return platformAndVersionMustBeIgnored;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlatformAndVersionValidationResult that = (PlatformAndVersionValidationResult) o;
            return platformAndVersionMustBeIgnored == that.platformAndVersionMustBeIgnored;
        }

        @Override
        public int hashCode() {
            return Objects.hash(platformAndVersionMustBeIgnored);
        }
    }
}