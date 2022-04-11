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
import java.util.Locale;
import java.util.Optional;

public final class ApplicationTypeHelper {
    private ApplicationTypeHelper() {
    }

    public static boolean isSupported(String applicationType) {
        try {
            fromValue(applicationType);
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    public static boolean isNativeApplication(String applicationType, Collection<ApplicationType> webApplications) {
        try {
            final var type = fromValue(applicationType);
            return !webApplications.contains(type);
        } catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    public static boolean isWebApplication(String applicationType, Collection<ApplicationType> webApplications) {
        try {
            final var type = fromValue(applicationType);
            return webApplications.contains(type);
        } catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    private static ApplicationType fromValue(String applicationType) {
        String lowercaseType = Optional.ofNullable(applicationType).map(type -> type.toLowerCase(Locale.ROOT)).orElse("");
        return ApplicationType.fromValue(lowercaseType);
    }

}