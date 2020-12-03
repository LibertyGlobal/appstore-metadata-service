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

import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerForUpdate;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maintainers")
public class MaintainersController {

    private static final Logger LOG = LoggerFactory.getLogger(MaintainersController.class);

    @Autowired
    private MaintainersService maintainersService;

    @GetMapping(value = "/{maintainerCode}", produces = {"application/json"})
    public ResponseEntity<Maintainer> getMaintainer(@PathVariable(value = "maintainerCode") String maintainerCode) {

        LOG.info("GET /maintainers/{maintainerCode} called with the following parameter: maintainerCode = '{}'", maintainerCode);

        final Maintainer maintainer = maintainersService.getMaintainer(maintainerCode);

        LOG.info("Returning: {}", maintainer);

        return ResponseEntity.ok(maintainer);
    }

    @PostMapping(produces = {"application/json"})
    public ResponseEntity<Void> createMaintainer(@Valid @RequestBody Maintainer maintainer) {
        LOG.info("POST /maintainers called with the following parameter: maintainer = '{}'", maintainer);

        maintainersService.createMaintainer(maintainer);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/{maintainerCode}", produces = {"application/json"})
    public ResponseEntity<Void> updateMaintainer(@PathVariable("maintainerCode") String maintainerCode,
            @Valid @RequestBody MaintainerForUpdate maintainerForUpdate) {

        LOG.info(
                "PUT /maintainers/{maintainerCode} called with the following parameters: maintainerCode = '{}, maintainer = '{}'",
                maintainerCode, maintainerForUpdate);

        final boolean updated = maintainersService.updateMaintainer(maintainerCode, maintainerForUpdate);

        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping(value = "/{maintainerCode}", produces = {"application/json"})
    public ResponseEntity<Void> deleteMaintainer(@PathVariable("maintainerCode") String maintainerCode) {

        LOG.info("DELETE /maintainers/{maintainerCode} called with the following parameter: maintainerCode='{}'", maintainerCode);

        final boolean deleteResult = maintainersService.deleteMaintainer(maintainerCode);

        return deleteResult
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
