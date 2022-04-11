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
package com.lgi.appstore.metadata.model;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class AppIdWithType {
    private final String applicationId;
    private final String applicationType;

    public AppIdWithType(String applicationId, String applicationType) {
        this.applicationId = requireNonNull(applicationId, "applicationId");
        this.applicationType = applicationType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppIdWithType that = (AppIdWithType) o;
        return applicationType == that.applicationType && Objects.equals(applicationId, that.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationType, applicationId);
    }
}
