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

package com.lgi.appstore.metadata.api.maintainer;

import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.MaintainerApplicationDetails;
import com.lgi.appstore.metadata.model.MaintainerApplicationsList;
import com.lgi.appstore.metadata.model.Platform;

import java.util.Optional;

public interface AppsService {
    MaintainerApplicationsList listApplications(String maintainerCode, String name, String description, String version, String type, Platform platform, Category category, Integer offset, Integer limit);

    Optional<MaintainerApplicationDetails> getApplicationDetails(String maintainerCode, String appId, String version);

    Optional<MaintainerApplicationDetails> getApplicationDetails(String maintainerCode, String appId);

    void addApplication(String maintainerCode, Application application);

    boolean updateLatestApplication(String maintainerCode, String appId, ApplicationForUpdate applicationForUpdate);

    boolean updateApplication(String maintainerCode, String appId, String version, ApplicationForUpdate applicationForUpdate);

    boolean deleteApplication(String maintainerCode, String appId, String version);

    boolean deleteLatestApplication(String maintainerCode, String appId);

    boolean deleteAllApplicationVersions(String maintainerCode, String appId);
}