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

import com.lgi.appstore.metadata.model.AppIdWithVersion;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.MaintainerApplicationDetails;
import com.lgi.appstore.metadata.model.MaintainerApplicationsList;
import com.lgi.appstore.metadata.model.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/maintainers/{maintainerCode}")
class MaintainerAppsController {

    @Autowired
    private AppsService appsService;

    private static final Logger LOG = LoggerFactory.getLogger(MaintainerAppsController.class);

    @GetMapping(value = "/apps/{appId:.+}",
            produces = {"application/json"})
    public ResponseEntity<MaintainerApplicationDetails> getApplicationDetails(@PathVariable("maintainerCode") String maintainerCode,
                                                                              @PathVariable("appId") String appId) {

        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appId);

        LOG.info("GET /maintainers/{maintainerCode}/apps/{appId} called with the following parameters: maintainerCode = '{}', appId = '{}', version = '{}'", maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion());

        final Optional<MaintainerApplicationDetails> applicationDetails = appIdWithVersion.isLatest()
                ? appsService.getApplicationDetails(maintainerCode, appIdWithVersion.getAppId())
                : appsService.getApplicationDetails(maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion());

        LOG.info("Returning: {}", applicationDetails);

        return applicationDetails.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/apps",
            produces = {"application/json"})
    public ResponseEntity<MaintainerApplicationsList> listApplications(@PathVariable(value = "maintainerCode") String maintainerCode,
                                                                       @Valid @RequestParam(value = "name", required = false) String name,
                                                                       @Valid @RequestParam(value = "description", required = false) String description,
                                                                       @Valid @RequestParam(value = "version", required = false) String version,
                                                                       @Valid @RequestParam(value = "type", required = false) String type,
                                                                       @Valid @RequestParam(value = "platform", required = false) Platform platform,
                                                                       @Valid @RequestParam(value = "category", required = false) Category category,
                                                                       @Valid @RequestParam(value = "offset", required = false) Integer offset,
                                                                       @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        LOG.info("GET /maintainers/{maintainerCode}/apps called with the following parameters: maintainerCode = '{}', name = '{}', description = '{}', version = '{}', type = '{}', platform = '{}', category = '{}', offset = '{}', limit = '{}'",
                maintainerCode, name, description, version, type, platform, category, offset, limit);

        final MaintainerApplicationsList applicationsList = appsService.listApplications(maintainerCode, name, description, version, type, platform, category, offset, limit);

        LOG.info("Returning: {}", applicationsList);

        return ResponseEntity.ok(applicationsList);
    }

    @PostMapping(value = "/apps",
            produces = {"application/json"})
    public ResponseEntity<Void> addApplication(@PathVariable("maintainerCode") String maintainerCode,
                                               @Valid @RequestBody Application application) {

        LOG.info("POST /maintainers/{maintainerCode}/apps called with the following parameters: maintainerCode = '{}', application = '{}'", maintainerCode, application);

        appsService.addApplication(maintainerCode, application);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/apps/{appId:.+}",
            produces = {"application/json"})
    public ResponseEntity<Void> updateApplication(@PathVariable("maintainerCode") String maintainerCode,
                                                  @PathVariable("appId") String appId,
                                                  @Valid @RequestBody ApplicationForUpdate applicationForUpdate) {

        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appId);

        LOG.info("PUT /maintainers/{maintainerCode}/apps/{appId} called with the following parameters: maintainerCode = '{}, appId = '{}', version = '{}', application = '{}'", maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion(), applicationForUpdate);

        final boolean updated = appIdWithVersion.isLatest()
                ? appsService.updateLatestApplication(maintainerCode, appIdWithVersion.getAppId(), applicationForUpdate)
                : appsService.updateApplication(maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion(), applicationForUpdate);

        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping(value = "/apps/{appId:.+}",
            produces = {"application/json"})
    public ResponseEntity<Void> deleteApplication(@PathVariable("maintainerCode") String maintainerCode,
                                                  @PathVariable("appId") String appId) {

        final AppIdWithVersion appIdWithVersion = AppIdWithVersion.fromString(appId);

        LOG.info("DELETE /maintainers/{maintainerCode}/apps/{appId} called with the following parameters: maintainerCode='{}', appId={}, version={}", maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion());

        final boolean deleteResult;
        if (appIdWithVersion.isAll()) {
            deleteResult = appsService.deleteAllApplicationVersions(maintainerCode, appIdWithVersion.getAppId());
        } else if (appIdWithVersion.isLatest()) {
            deleteResult = appsService.deleteLatestApplication(maintainerCode, appIdWithVersion.getAppId());
        } else {
            deleteResult = appsService.deleteApplication(maintainerCode, appIdWithVersion.getAppId(), appIdWithVersion.getVersion());
        }

        return deleteResult
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}