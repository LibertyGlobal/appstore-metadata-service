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

package com.lgi.appstore.metadata.api.stb;

import com.lgi.appstore.metadata.model.AppIdWithVersion;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.StbApplicationDetails;
import com.lgi.appstore.metadata.model.StbApplicationsList;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

//@Timed
@RestController
@RequestMapping("/apps")
public class StbAppsController {

    @Autowired
    private AppsService appsService;

    private static final Logger LOG = LoggerFactory.getLogger(StbAppsController.class);

    @GetMapping(value = "/{appId:.+}",
            produces = {"application/json"})
    public ResponseEntity<StbApplicationDetails> getApplicationDetails(@PathVariable("appId") String appId) {

        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appId);

        LOG.info("GET /apps/{appId} called with the following parameters: appId = '{}', version = '{}'", appIdWithVersion.getAppId(), appIdWithVersion.getVersion());

        final Optional<StbApplicationDetails> applicationDetails = appIdWithVersion.isLatest()
                ? appsService.getApplicationDetails(appIdWithVersion.getAppId())
                : appsService.getApplicationDetails(appIdWithVersion.getAppId(), appIdWithVersion.getVersion());

        LOG.info("Returning: {}", applicationDetails);

        return applicationDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<StbApplicationsList> listApplications(@Valid @RequestParam(value = "name", required = false) String name,
                                                                @Valid @RequestParam(value = "description", required = false) String description,
                                                                @Valid @RequestParam(value = "version", required = false) String version,
                                                                @Valid @RequestParam(value = "type", required = false) String type,
                                                                @Valid @RequestParam(value = "platform", required = false) Platform platform,
                                                                @Valid @RequestParam(value = "category", required = false) Category category,
                                                                @Valid @RequestParam(value = "maintainerName", required = false) String maintainerName,
                                                                @Valid @RequestParam(value = "offset", required = false) Integer offset,
                                                                @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        LOG.info("GET /apps called");

        final StbApplicationsList applicationsList = appsService.listApplications(name, description, version, type, platform, category, maintainerName, offset, limit);

        LOG.info("Returning: {}", applicationsList);

        return ResponseEntity.ok(applicationsList);
    }
}
