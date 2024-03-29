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
package com.lgi.appstore.metadata.api.stb;

import com.lgi.appstore.metadata.model.AppIdWithType;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.StbApplicationDetails;
import com.lgi.appstore.metadata.model.StbApplicationsList;

import java.util.Optional;

public interface AppsService {
    Optional<AppIdWithType> getApplicationType(String appId);

    Optional<AppIdWithType> getApplicationType(String appId, String version);

    StbApplicationsList listApplications(String name, String description, String version, String type, Platform platform, Category category, String maintainerName, Integer offset, Integer limit);

    Optional<StbApplicationDetails> getApplicationDetails(String appId, String version, String platformName, String firmwareVer);

    Optional<StbApplicationDetails> getApplicationDetails(String appId, String platformName, String firmwareVer);
}
